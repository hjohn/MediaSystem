package hs.mediasystem.fs;

import hs.mediasystem.db.CachedItemEnricher;
import hs.mediasystem.db.Identifier;
import hs.mediasystem.db.IdentifyException;
import hs.mediasystem.db.Item;
import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemEvent;
import javafx.application.Platform;
import javafx.event.EventHandler;

import javax.inject.Inject;

public class MediaItemEnrichmentEventHandler implements EventHandler<MediaItemEvent> {
  private final CachedItemEnricher cachedItemEnricher;

  @Inject
  public MediaItemEnrichmentEventHandler(CachedItemEnricher cachedItemEnricher) {
    this.cachedItemEnricher = cachedItemEnricher;
  }

  public void enrich(final MediaItem mediaItem, final boolean bypassCache) {
    new Thread("MediaItemEnrichmentEventHandler") {
      @Override
      public void run() {
        try {
          LocalInfo localInfo = mediaItem.getLocalInfo();
          Identifier identifier = cachedItemEnricher.identifyItem(localInfo, bypassCache);

          final Item item = cachedItemEnricher.loadItem(identifier, bypassCache);

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
      }
    }.start();
  }

  @Override
  public void handle(MediaItemEvent event) {
    enrich(event.getMediaItem(), false);
  }
}
