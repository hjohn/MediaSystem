package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.db.TvdbEpisodeEnricher;
import hs.mediasystem.db.TvdbSerieEnricher;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaNodeCellProviderRegistry;
import hs.mediasystem.fs.EpisodeComparator;
import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.fs.SeasonGrouper;
import hs.mediasystem.fs.Serie;
import hs.mediasystem.fs.SeriesMediaTree;
import hs.mediasystem.media.Episode;
import hs.mediasystem.media.Media;
import hs.mediasystem.screens.BannerCell;
import hs.mediasystem.screens.EpisodeCell;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MediaItemEnrichmentEventHandler;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.StandardLayout;
import hs.mediasystem.screens.StandardLayout.MediaGroup;
import hs.mediasystem.screens.ProgramController;

import java.nio.file.Paths;

import javafx.scene.image.Image;

import javax.inject.Inject;
import javax.inject.Provider;

public class SeriesMainMenuExtension implements MainMenuExtension {
  private final Provider<SelectMediaPresentation> selectMediaPresentationProvider;
  private final MediaItemEnrichmentEventHandler enrichmentHandler;

  @Inject
  public SeriesMainMenuExtension(Provider<SelectMediaPresentation> selectMediaPresentationProvider, MediaItemEnrichmentEventHandler enrichmentHandler) {
    this.selectMediaPresentationProvider = selectMediaPresentationProvider;
    this.enrichmentHandler = enrichmentHandler;

    TvdbSerieEnricher serieEnricher = new TvdbSerieEnricher();

    TypeBasedItemEnricher.registerEnricher("Serie", serieEnricher);
    TypeBasedItemEnricher.registerEnricher("Episode", new TvdbEpisodeEnricher(serieEnricher));
    StandardView.registerLayout(SeriesMediaTree.class, MediaRootType.SERIES);
    StandardView.registerLayout(Serie.class, MediaRootType.SERIE_EPISODES);
    MediaNodeCellProviderRegistry.register(MediaNodeCellProviderRegistry.HORIZONTAL_CELL, hs.mediasystem.media.Serie.class, new Provider<BannerCell>() {
      @Override
      public BannerCell get() {
        return new BannerCell();
      }
    });
    MediaNodeCellProviderRegistry.register(MediaNodeCellProviderRegistry.HORIZONTAL_CELL, Episode.class, new Provider<EpisodeCell>() {
      @Override
      public EpisodeCell get() {
        return new EpisodeCell();
      }
    });

    StandardLayout.registerMediaGroup(Serie.class, new MediaGroup(new SeasonGrouper(), EpisodeComparator.INSTANCE, true, true) {
      @Override
      public Media createMediaFromFirstItem(MediaItem item) {
        Integer season = item.get(Episode.class).getSeason();

        return new Media(season == null || season == 0 ? "Specials" : "Season " + season);
      }

      @Override
      public String getShortTitle(MediaItem item) {
        Integer season = item.get(Episode.class).getSeason();

        return season == null || season == 0 ? "Sp." : "" + season;
      }
    });
  }

  @Override
  public String getTitle() {
    return "Series";
  }

  @Override
  public Image getImage() {
    return new Image("images/aktion.png");
  }

  @Override
  public Destination getDestination(final ProgramController controller) {
    return new Destination(getTitle()) {
      private SelectMediaPresentation presentation;
      private SeriesMediaTree mediaTree;

      @Override
      protected void init() {
        presentation = selectMediaPresentationProvider.get();
      }

      @Override
      protected void intro() {
        controller.showScreen(presentation.getView());
        if(mediaTree == null) {
          mediaTree = new SeriesMediaTree(Paths.get(controller.getIni().getValue("general", "series.path")));
          mediaTree.onItemQueued().set(enrichmentHandler);
          presentation.setMediaTree(mediaTree);
        }
      }
    };
  }

  @Override
  public double order() {
    return 0.2;
  }
}
