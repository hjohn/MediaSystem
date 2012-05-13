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
import java.util.Map;

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
  private final Map<MediaItem, Task<?>> enrichTasks = new HashMap<>();

  @Inject
  public MediaItemEnrichmentEventHandler(TypeBasedItemEnricher typeBasedItemEnricher, ItemsDao itemsDao, MessagePaneExecutionQueue executorService) {
    this.typeBasedItemEnricher = typeBasedItemEnricher;
    this.itemsDao = itemsDao;
    this.slowExecutionQueue = executorService;
  }

  public void enrich(final MediaItem mediaItem) {
    final CachedEnrichTask task = new CachedEnrichTask(mediaItem);

    task.stateProperty().addListener(new TaskChangeListener(task, mediaItem));

    enrichTasks.put(mediaItem, task);
    fastExecutionQueue.submit(task);
  }

  public void enrichNoCache(final MediaItem mediaItem, Identifier identifier) {
    final TypeBasedEnrichTask task = new TypeBasedEnrichTask(mediaItem, identifier);

    task.stateProperty().addListener(new TaskChangeListener(task, mediaItem));

    enrichTasks.put(mediaItem, task);
    slowExecutionQueue.submit(task);
  }

  @Override
  public void handle(MediaItemEvent event) {
    synchronized(enrichTasks) {
      MediaItem mediaItem = event.getMediaItem();
      MediaItem.State state = mediaItem.getState();

      if(state == MediaItem.State.STANDARD) {
        enrich(mediaItem);
      }
      else if(state == MediaItem.State.QUEUED) {
        Task<?> task = enrichTasks.get(mediaItem);

        if(task != null) {
          if(task instanceof CachedEnrichTask) {
            fastExecutionQueue.promote(task);
          }
          else {
            slowExecutionQueue.promote(task);
          }
        }
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
    private final Task<?> task;
    private final MediaItem mediaItem;

    private TaskChangeListener(Task<?> task, MediaItem mediaItem) {
      this.task = task;
      this.mediaItem = mediaItem;
    }

    @Override
    public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
      if(newValue == State.FAILED) {
        System.out.println("[WARN] Worker " + task.getTitle() + " failed with exception: " + task.getException());
        task.getException().printStackTrace(System.out);
      }
      if(newValue == State.FAILED || newValue == State.CANCELLED || newValue == State.SUCCEEDED) {
        enrichTasks.remove(mediaItem);
      }
      if(newValue == State.SUCCEEDED) {
        if(task instanceof CachedEnrichTask && !((CachedEnrichTask)task).getValue()) {
          enrichNoCache(mediaItem, ((CachedEnrichTask)task).getIdentifier());
        }
      }
    }
  }

  private final class CachedEnrichTask extends Task<Boolean> {
    private final MediaItem mediaItem;

    private Identifier identifier;

    public CachedEnrichTask(MediaItem mediaItem) {
      this.mediaItem = mediaItem;

      updateProgress(0, 2);
      updateTitle(mediaItem.getTitle());
    }

    public Identifier getIdentifier() {
      return identifier;
    }

    @Override
    public Boolean call() {
      try {
        @SuppressWarnings("unchecked")
        LocalInfo<Object> localInfo = (LocalInfo<Object>)mediaItem.getLocalInfo();

        identifier = identifyItem(localInfo);

        updateProgress(1, 2);

        Item item = loadItem(identifier);

        if(item.getVersion() < ItemsDao.VERSION) {
          return false;
        }

        enrich(mediaItem, item);

        updateProgress(2, 2);

        return true;
      }
      catch(ItemNotFoundException e) {
        return false;
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

  private final class TypeBasedEnrichTask extends Task<Void> {
    private final MediaItem mediaItem;

    private Identifier identifier;

    public TypeBasedEnrichTask(MediaItem mediaItem, Identifier identifier) {
      this.mediaItem = mediaItem;
      this.identifier = identifier;

      updateProgress(0, 5);
      updateTitle(mediaItem.getTitle());
    }

    @Override
    public Void call() {
      try {
        @SuppressWarnings("unchecked")
        LocalInfo<Object> localInfo = (LocalInfo<Object>)mediaItem.getLocalInfo();

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

        enrich(mediaItem, item);

        updateProgress(5, 5);

        return null;
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
