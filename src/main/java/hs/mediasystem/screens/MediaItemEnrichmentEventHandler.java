package hs.mediasystem.screens;

import hs.mediasystem.db.CachedItemEnricher;
import hs.mediasystem.db.Identifier;
import hs.mediasystem.db.IdentifyException;
import hs.mediasystem.db.Item;
import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.Source;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItem.State;
import hs.mediasystem.framework.MediaItemEvent;
import hs.mediasystem.fs.SourceImageHandle;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;

import javax.inject.Inject;

public class MediaItemEnrichmentEventHandler implements EventHandler<MediaItemEvent> {
  private final CachedItemEnricher cachedItemEnricher;
  private final MessagePaneExecutorService executorService;

  @Inject
  public MediaItemEnrichmentEventHandler(CachedItemEnricher cachedItemEnricher, MessagePaneExecutorService executorService) {
    this.cachedItemEnricher = cachedItemEnricher;
    this.executorService = executorService;
  }

  public void enrich(final MediaItem mediaItem, final boolean bypassCache) {
    executorService.execute(mediaItem, new Task<Void>() {
      @Override
      public Void call() {
        updateTitle("Fetching metadata");
        updateProgress(0, 2);

        try {
          @SuppressWarnings("unchecked")
          LocalInfo<Object> localInfo = (LocalInfo<Object>)mediaItem.getLocalInfo();
          String message = localInfo.getTitle();

          if(message == null) {
            message = localInfo.getGroupName();
          }

          if(localInfo.getSeason() != null) {
            message += " " + localInfo.getSeason() + "x" + localInfo.getEpisode();
          }
          else if(localInfo.getEpisode() != null && localInfo.getEpisode() > 1) {
            message += " " + localInfo.getEpisode();
          }

          updateMessage(message);

          Identifier identifier = cachedItemEnricher.identifyItem(localInfo, bypassCache);

          updateProgress(1, 2);

          final Item item = cachedItemEnricher.loadItem(identifier, localInfo, bypassCache);

          updateProgress(2, 2);

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
              mediaItem.setEnriched();
            }
          });
        }
        catch(IdentifyException e) {
          System.out.println("[FINE] MediaItemEnrichmentEventHandler.enrich() - Enrichment failed of " + mediaItem + " failed with exception: " + e);
          e.printStackTrace(System.out);
        }

        return null;
      }
    });
  }

  @Override
  public void handle(MediaItemEvent event) {
    MediaItem mediaItem = event.getMediaItem();
    State state = mediaItem.getState();

    if(state == MediaItem.State.STANDARD) {
      enrich(mediaItem, false);
    }
    else if(state == MediaItem.State.QUEUED) {
      executorService.promote(mediaItem);
    }
  }

  private static SourceImageHandle createImageHandle(Source<byte[]> source, Item item, String keyPostFix) {
    String key = item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getSubtitle() + "-" + keyPostFix;

    return source == null ? null : new SourceImageHandle(source, key);
  }
}
