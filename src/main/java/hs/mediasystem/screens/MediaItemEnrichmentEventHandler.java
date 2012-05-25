package hs.mediasystem.screens;

import hs.mediasystem.db.EnricherMatch;
import hs.mediasystem.db.Identifier;
import hs.mediasystem.db.IdentifyException;
import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.MediaHash;
import hs.mediasystem.db.MediaId;
import hs.mediasystem.db.Source;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemEvent;
import hs.mediasystem.fs.SourceImageHandle;
import hs.mediasystem.media.Media;
import hs.mediasystem.media.Movie;
import hs.mediasystem.media.Serie;
import hs.mediasystem.util.ExecutionQueue;
import hs.mediasystem.util.ThreadPoolExecutionQueue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;
import javafx.event.EventHandler;

import javax.inject.Inject;

/**
 * Enriches a MediaItem asynchronously, by first looking for cached enriched content.  If not
 * found there the task is off-loaded in a new Task to the {@link TypeBasedItemEnricher} using
 * the {@link MessagePaneExecutionQueue}.
 *
 * In effect this means that cached enrichments are handled invisibly, while slower meta-data
 * lookups are shown by means of the MessagePaneExecutorService.
 */
public class MediaItemEnrichmentEventHandler implements EventHandler<MediaItemEvent> {
  private final TypeBasedItemEnricher typeBasedItemEnricher;
  private final ItemsDao itemsDao;
  private final ExecutionQueue slowExecutionQueue;
  private final ExecutionQueue fastExecutionQueue = new ThreadPoolExecutionQueue(5);
  private final Map<String, EnrichTask> enrichTasks = new HashMap<>();

  @Inject
  public MediaItemEnrichmentEventHandler(TypeBasedItemEnricher typeBasedItemEnricher, ItemsDao itemsDao, MessagePaneExecutionQueue executorService) {
    this.typeBasedItemEnricher = typeBasedItemEnricher;
    this.itemsDao = itemsDao;
    this.slowExecutionQueue = executorService;
  }

  @Override
  public void handle(MediaItemEvent event) {
    enrichInternal(event.getMediaItem(), false);
  }

  public void enrich(final MediaItem mediaItem) {
    enrichInternal(mediaItem, false);
  }

  public void enrichNoCache(final MediaItem mediaItem) {
    enrichInternal(mediaItem, true);
  }

  private void enrichInternal(final MediaItem mediaItem, boolean bypassCache) {
    synchronized(enrichTasks) {
      EnrichTask task = enrichTasks.get(mediaItem.getUri());

      if(task == null) {
        if(bypassCache) {
          submitTask(new TypeBasedEnrichTask(mediaItem), mediaItem.getUri());
        }
        else {
          submitTask(new CachedEnrichTask(mediaItem), mediaItem.getUri());
        }
      }
      else {
        task.addMediaItem(mediaItem);

        if(task instanceof CachedEnrichTask) {
          fastExecutionQueue.promote(task);
        }
        else {
          slowExecutionQueue.promote(task);
        }
      }
    }
  }

  private void submitTask(EnrichTask task, String uri) {
    synchronized(enrichTasks) {
      task.stateProperty().addListener(new TaskChangeListener(task, uri));

      enrichTasks.put(uri, task);

      if(task instanceof CachedEnrichTask) {
        fastExecutionQueue.submit(task);
      }
      else {
        slowExecutionQueue.submit(task);
      }
    }
  }

  private static SourceImageHandle createImageHandle(Source<byte[]> source, Item item, String keyPostFix) {
    String key = "MediaItemEnrichmentEventHandler:/" + item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getSubtitle() + "-" + item.getImdbId() + "-" + keyPostFix;

    return source == null ? null : new SourceImageHandle(source, key);
  }

  private static void enrich(final MediaItem mediaItem, final Item item) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        Media media = mediaItem.get(Media.class);
        Movie movie = mediaItem.get(Movie.class);
        Serie serie = mediaItem.get(Serie.class);

        if(mediaItem.getMediaType().equals("Episode")) {
          media.titleProperty().set(item.getTitle());
        }
        media.backgroundProperty().set(createImageHandle(item.getBackground(), item, "background"));
        media.imageProperty().set(createImageHandle(item.getPoster(), item, "poster"));
        media.descriptionProperty().set(item.getPlot());
        media.ratingProperty().set(item.getRating());
        media.runtimeProperty().set(item.getRuntime());
        media.genresProperty().set(item.getGenres());
        media.releaseDateProperty().set(item.getReleaseDate());

        if(serie != null) {
          serie.bannerProperty().set(createImageHandle(item.getBanner(), item, "banner"));
        }
        if(movie != null) {
          movie.languageProperty().set(item.getLanguage());
          movie.tagLineProperty().set(item.getTagline());
          movie.imdbNumberProperty().set(item.getImdbId());
        }

        mediaItem.viewedProperty().set(item.isViewed());
//        mediaItem.matchAccuracyProperty().set(item.getMatchAccuracy());
//        mediaItem.resumePositionProperty().set(item.getResumePosition());
        mediaItem.setDatabaseId(item.getId());
      }
    });
  }

  private final class TaskChangeListener implements ChangeListener<State> {
    private final EnrichTask task;
    private final String uri;

    private TaskChangeListener(EnrichTask task, String uri) {
      this.task = task;
      this.uri = uri;
    }

    @Override
    public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
      if(newValue == State.FAILED || newValue == State.CANCELLED || newValue == State.SUCCEEDED) {
        Item item = task.getValue();

        if(newValue == State.FAILED) {
          System.out.println("[WARN] Worker " + task.getTitle() + " failed with exception: " + task.getException());
          task.getException().printStackTrace(System.out);
        }

        if(newValue == State.SUCCEEDED && item != null) {
          for(MediaItem mediaItem : task.getMediaItems()) {
            mediaItem.setEnriched();
            enrich(mediaItem, item);
          }
        }

        synchronized(enrichTasks) {
          /*
           * The removal of the task must be after the MediaItems have been set to Enriched as otherwise they may trigger another enrich.
           * The removal (of a CachedEnrichTask) must also be before the new TypeBasedEnrichTask is submitted as otherwise the new task
           * will be removed too early by this code.
           */
          enrichTasks.remove(uri);
        }

        if(newValue == State.SUCCEEDED && item == null) {
          if(task instanceof CachedEnrichTask) {
            submitTask(new TypeBasedEnrichTask((CachedEnrichTask)task), uri);
          }
        }
      }
    }
  }

  private abstract class EnrichTask extends Task<Item> {
    protected final Set<MediaItem> mediaItems;
    protected final MediaItem mediaItem;

    public EnrichTask(String title, MediaItem mediaItem, Set<MediaItem> mediaItems) {
      this.mediaItems = mediaItems;
      this.mediaItem = mediaItem;

      updateTitle(title);
    }

    public EnrichTask(final MediaItem mediaItem) {
      this(mediaItem.getTitle(), mediaItem, new HashSet<MediaItem>() {{
        add(mediaItem);
      }});
    }

    public void addMediaItem(MediaItem mediaItem) {
      mediaItems.add(mediaItem);
    }

    public Set<MediaItem> getMediaItems() {
      return mediaItems;
    }
  }

  private final class CachedEnrichTask extends EnrichTask {
    private MediaData mediaData;

    public CachedEnrichTask(final MediaItem mediaItem) {
      super(mediaItem);

      updateProgress(0, 2);
    }

    public MediaData getMediaData() {
      return mediaData;
    }

    @Override
    public Item call() throws IOException {
      try {
        mediaData = identifyItem(mediaItem);

        updateProgress(1, 2);

        Item item = loadItem(mediaData.getIdentifier());

        if(item.getVersion() < ItemsDao.VERSION) {
          return null;
        }

        updateProgress(2, 2);

        return item;
      }
      catch(ItemNotFoundException e) {
        return null;
      }
    }

    public MediaData identifyItem(MediaItem mediaItem) throws ItemNotFoundException {
      MediaData mediaData = itemsDao.getMediaDataByUri(mediaItem.getUri());

      if(mediaData == null) {
        MediaId mediaId = createMediaId(mediaItem.getUri());

        mediaItem.setMediaId(mediaId);
        mediaData = itemsDao.getMediaDataByHash(mediaId.getHash());

        if(mediaData == null) {
          throw new ItemNotFoundException("Unable to get MediaData by uri or hash");
        }

        mediaData.setUri(mediaItem.getUri());  // replace uri, as it didn't match anymore
        mediaData.setMediaId(mediaId);  // replace mediaId, even though hash matches, to update the other values just in case

        itemsDao.updateMediaData(mediaData);
      }

      return mediaData;
    }


    public Item loadItem(Identifier identifier) throws ItemNotFoundException {
      System.out.println("[FINE] MediaItemEnrichmentEventHandler.CachedEnrichTask.loadItem() - Loading from Cache: " + identifier);
      Item item = itemsDao.loadItem(identifier);

      System.out.println("[FINE] MediaItemEnrichmentEventHandler.CachedEnrichTask.loadItem() - Succesfully loaded: " + item);

      return item;
    }
  }

  private final class TypeBasedEnrichTask extends EnrichTask {
    private boolean bypassCache;
    private MediaData mediaData;

    public TypeBasedEnrichTask(MediaItem mediaItem) {
      super(mediaItem);

      updateProgress(0, 5);
      updateTitle(mediaItem.getTitle());

      bypassCache = true;
    }

    public TypeBasedEnrichTask(CachedEnrichTask task) {
      super(task.getTitle(), task.mediaItem, task.mediaItems);

      this.mediaData = task.getMediaData();
    }

    @Override
    public Item call() {
      try {
        if(mediaData == null) {
          EnricherMatch enricherMatch = typeBasedItemEnricher.identifyItem(mediaItem);

          updateProgress(1, 5);

          MediaId mediaId = mediaItem.getMediaId() == null ? createMediaId(mediaItem.getUri()) : mediaItem.getMediaId();

          mediaData = new MediaData();

          mediaData.setIdentifier(enricherMatch.getIdentifier());
          mediaData.setMediaId(mediaId);
          mediaData.setUri(mediaItem.getUri());
          mediaData.setMatchType(enricherMatch.getMatchType());
          mediaData.setMatchAccuracy(enricherMatch.getMatchAccuracy());

          itemsDao.storeMediaData(mediaData);

          updateProgress(2, 5);
        }

        Item oldItem;

        try {
          oldItem = itemsDao.loadItem(mediaData.getIdentifier());
        }
        catch(ItemNotFoundException e) {
          oldItem = null;
        }

        updateProgress(3, 5);

        Item item = bypassCache || oldItem == null ? typeBasedItemEnricher.loadItem(mediaData.getIdentifier(), mediaItem) : oldItem;

        updateProgress(4, 5);

        if(!item.equals(oldItem)) {
          if(oldItem != null) {
            item.setId(oldItem.getId());
            itemsDao.updateItem(item);
          }
          else {
            itemsDao.storeItem(item);
          }
        }

        updateProgress(5, 5);

        return item;
      }
      catch(IdentifyException e) {
        throw new RuntimeException(e);
      }
      catch(ItemNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private MediaId createMediaId(String uri) {
    try {
      Path path = Paths.get(uri);
      boolean isDirectory = Files.isDirectory(path);

      return new MediaId(
        isDirectory ? 0 : Files.size(path),
        Files.getLastModifiedTime(path).toMillis(),
        isDirectory ? null : MediaHash.loadMediaHash(path),
        isDirectory ? null : MediaHash.loadOpenSubtitlesHash(path)
      );
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
