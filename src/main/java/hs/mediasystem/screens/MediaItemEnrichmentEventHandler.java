package hs.mediasystem.screens;

import hs.mediasystem.db.Identifier;
import hs.mediasystem.db.IdentifyException;
import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.Source;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemEvent;
import hs.mediasystem.fs.SourceImageHandle;
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
        mediaItem.setImdbId(item.getImdbId());
        mediaItem.officialTitleProperty().set(item.getTitle());
        mediaItem.backgroundProperty().set(createImageHandle(item.getBackground(), item, "background"));
        mediaItem.bannerProperty().set(createImageHandle(item.getBanner(), item, "banner"));
        mediaItem.posterProperty().set(createImageHandle(item.getPoster(), item, "poster"));
        mediaItem.plotProperty().set(item.getPlot());
        mediaItem.ratingProperty().set(item.getRating());
        mediaItem.releaseDateProperty().set(item.getReleaseDate());
        mediaItem.genresProperty().set(item.getGenres());
        mediaItem.setLanguage(item.getLanguage());
        mediaItem.setTagline(item.getTagline());
        mediaItem.runtimeProperty().set(item.getRuntime());
        mediaItem.viewedProperty().set(item.isViewed());
        mediaItem.matchAccuracyProperty().set(item.getMatchAccuracy());
        mediaItem.resumePositionProperty().set(item.getResumePosition());
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
    protected final LocalInfo<Object> localInfo;

    public EnrichTask(String title, LocalInfo<Object> localInfo, Set<MediaItem> mediaItems) {
      this.mediaItems = mediaItems;
      this.localInfo = localInfo;

      updateTitle(title);
    }

    @SuppressWarnings("unchecked")
    public EnrichTask(final MediaItem mediaItem) {
      this(mediaItem.getTitle(), (LocalInfo<Object>)mediaItem.getLocalInfo(), new HashSet<MediaItem>() {{
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
        identifier = identifyItem(localInfo);

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

    public Identifier identifyItem(LocalInfo<Object> localInfo) throws ItemNotFoundException {
      System.out.println("[FINE] MediaItemEnrichmentEventHandler.CachedEnrichTask.identifyItem() - with surrogatename: " + localInfo.getSurrogateName());

      return itemsDao.loadIdentifier(localInfo.getSurrogateName());
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
      super(task.getTitle(), task.localInfo, task.mediaItems);

      this.identifier = task.getIdentifier();
    }

    @Override
    public Item call() {
      try {
        if(identifier == null) {
          identifier = typeBasedItemEnricher.identifyItem(localInfo);

          updateProgress(1, 5);

          itemsDao.storeAsQuery(localInfo.getSurrogateName(), identifier);

          updateProgress(2, 5);
        }

        Item item = typeBasedItemEnricher.loadItem(identifier, localInfo);

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
