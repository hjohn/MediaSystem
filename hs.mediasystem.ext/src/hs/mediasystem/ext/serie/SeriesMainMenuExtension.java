package hs.mediasystem.ext.serie;

import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.Episode;
import hs.mediasystem.framework.EpisodeBase;
import hs.mediasystem.framework.MediaNodeCellProviderRegistry;
import hs.mediasystem.framework.Serie;
import hs.mediasystem.framework.SerieBase;
import hs.mediasystem.framework.SerieItem;
import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.persist.Persister;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.BannerCell;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.screens.selectmedia.StandardView;

import java.nio.file.Paths;

import javafx.scene.image.Image;

import javax.inject.Provider;

public class SeriesMainMenuExtension implements MainMenuExtension {
  private volatile SelectMediaPresentationProvider selectMediaPresentationProvider;
  private volatile SerieEnricher serieEnricher;
  private volatile EpisodeEnricher episodeEnricher;
  private volatile EnrichCache enrichCache;
  private volatile Persister persister;

  public SeriesMainMenuExtension() {
    TvdbSerieEnricher tvdbSerieEnricher = new TvdbSerieEnricher();

    TypeBasedItemEnricher.registerEnricher(SerieBase.class, tvdbSerieEnricher);
    TypeBasedItemEnricher.registerEnricher(EpisodeBase.class, new TvdbEpisodeEnricher(tvdbSerieEnricher));

    StandardView.registerLayout(SeriesMediaTree.class, MediaRootType.SERIES);
    StandardView.registerLayout(SerieItem.class, MediaRootType.SERIE_EPISODES);
    MediaNodeCellProviderRegistry.register(MediaNodeCellProviderRegistry.HORIZONTAL_CELL, Serie.class, new Provider<BannerCell>() {
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
  }

  public void init() {
    enrichCache.registerEnricher(Serie.class, serieEnricher);
    enrichCache.registerEnricher(Episode.class, episodeEnricher);
  }

  @Override
  public String getTitle() {
    return "Series";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/serie/serie.png"));
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
          mediaTree = new SeriesMediaTree(enrichCache, persister, Paths.get(controller.getIni().getValue("general", "series.path")));
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
