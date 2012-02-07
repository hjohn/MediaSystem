package hs.mediasystem.fs;

import hs.mediasystem.db.CachedItemEnricher;
import hs.mediasystem.db.Identifier;
import hs.mediasystem.db.IdentifyException;
import hs.mediasystem.db.Item;
import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemEvent;
import hs.mediasystem.screens.MessagePaneExecutorService;
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
    executorService.execute(new Task<Void>() {
      @Override
      public Void call() {
        updateTitle("Fetching metadata");
        updateProgress(0, 2);

        try {
          LocalInfo localInfo = mediaItem.getLocalInfo();
          String message = localInfo.getTitle();

          if(localInfo.getSeason() != null) {
            message += " " + localInfo.getSeason() + "x" + localInfo.getEpisode();
          }
          else if(localInfo.getEpisode() != null && localInfo.getEpisode() > 1) {
            message += " " + localInfo.getEpisode();
          }

          updateMessage(message);

          Identifier identifier = cachedItemEnricher.identifyItem(localInfo, bypassCache);

          updateProgress(1, 2);

          final Item item = cachedItemEnricher.loadItem(identifier, bypassCache);

          updateProgress(2, 2);

          Platform.runLater(new Runnable() {
            @Override
            public void run() {
              mediaItem.titleProperty().set(item.getTitle());
              mediaItem.backgroundProperty().set(new SourceImageHandle(item.getBackground(), item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getSubtitle() + "-background"));
              mediaItem.bannerProperty().set(new SourceImageHandle(item.getBanner(), item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getSubtitle() + "-banner"));
              mediaItem.posterProperty().set(new SourceImageHandle(item.getPoster(), item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getSubtitle() + "-poster"));
              mediaItem.plotProperty().set(item.getPlot());
              mediaItem.ratingProperty().set(item.getRating());
              mediaItem.releaseDateProperty().set(item.getReleaseDate());
              mediaItem.genresProperty().set(item.getGenres());
              mediaItem.setLanguage(item.getLanguage());
              mediaItem.setTagline(item.getTagline());
              mediaItem.runtimeProperty().set(item.getRuntime());
              mediaItem.setState(MediaItem.State.ENRICHED);
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
    enrich(event.getMediaItem(), false);
  }
}