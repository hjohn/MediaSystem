package hs.mediasystem.screens;

import hs.mediasystem.db.Identifier;
import hs.mediasystem.db.IdentifyException;
import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemsDao;
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
        mediaItem.setEnriched();
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
      if(newValue == State.FAILED) {
        System.out.println("[WARN] Worker " + task.getTitle() + " failed with exception: " + task.getException());
        task.getException().printStackTrace(System.out);
      }
      if(newValue == State.FAILED || newValue == State.CANCELLED || newValue == State.SUCCEEDED) {
        synchronized(enrichTasks) {
          enrichTasks.remove(uri);
        }
      }
      if(newValue == State.SUCCEEDED) {
        Item item = task.getValue();

        if(item == null) {
          if(task instanceof CachedEnrichTask) {
            submitTask(new TypeBasedEnrichTask((CachedEnrichTask)task), uri);
          }
        }
        else {
          for(MediaItem mediaItem : task.getMediaItems()) {
            enrich(mediaItem, item);
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
    private Identifier identifier;

    public CachedEnrichTask(final MediaItem mediaItem) {
      super(mediaItem);

      updateProgress(0, 2);
    }

    public Identifier getIdentifier() {
      return identifier;
    }

    @Override
    public Item call() {
      try {
        identifier = identifyItem(mediaItem);

        updateProgress(1, 2);

        Item item = loadItem(identifier);

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

    public Identifier identifyItem(MediaItem mediaItem) throws ItemNotFoundException {
      System.out.println("[FINE] MediaItemEnrichmentEventHandler.CachedEnrichTask.identifyItem() - with key: " + mediaItem.getMedia().getKey());

      return itemsDao.loadIdentifier(mediaItem.getMedia().getKey());
    }

    public Item loadItem(Identifier identifier) throws ItemNotFoundException {
      System.out.println("[FINE] MediaItemEnrichmentEventHandler.CachedEnrichTask.loadItem() - Loading from Cache: " + identifier);
      Item item = itemsDao.loadItem(identifier);

      System.out.println("[FINE] MediaItemEnrichmentEventHandler.CachedEnrichTask.loadItem() - Succesfully loaded: " + item);

      return item;
    }
  }

  private final class TypeBasedEnrichTask extends EnrichTask {
    private Identifier identifier;

    public TypeBasedEnrichTask(MediaItem mediaItem) {
      super(mediaItem);

      updateProgress(0, 5);
      updateTitle(mediaItem.getTitle());
    }

    public TypeBasedEnrichTask(CachedEnrichTask task) {
      super(task.getTitle(), task.mediaItem, task.mediaItems);

      this.identifier = task.getIdentifier();
    }

    @Override
    public Item call() {
      try {
        if(identifier == null) {
          identifier = typeBasedItemEnricher.identifyItem(mediaItem);

          updateProgress(1, 5);

          itemsDao.storeAsQuery(mediaItem.getMedia().getKey(), identifier);

          updateProgress(2, 5);
        }

        Item item = typeBasedItemEnricher.loadItem(identifier, mediaItem);  // TODO identifier once found may actually refer to existing data, no need to load it then (unless caching unwanted)

        updateProgress(3, 5);

        Item oldItem;

        try {
          oldItem = itemsDao.loadItem(identifier);
        }
        catch(ItemNotFoundException e) {
          oldItem = null;
        }

        updateProgress(4, 5);

        if(oldItem != null) {
          item.setId(oldItem.getId());
          itemsDao.updateItem(item);
        }
        else {
          itemsDao.storeItem(item);
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
}
