package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.IdentifierEnricher;
import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.screens.selectmedia.StandardView;

import java.nio.file.Paths;

import javafx.scene.image.Image;

public class SeriesMainMenuExtension implements MainMenuExtension {
  private volatile SelectMediaPresentationProvider selectMediaPresentationProvider;
  private volatile SerieEnricher serieEnricher;
  private volatile EpisodeEnricher episodeEnricher;
  private volatile EnrichCache enrichCache;
  private volatile PersistQueue persister;
  private volatile IdentifierDao identifierDao;

  public SeriesMainMenuExtension() {
    StandardView.registerLayout(SeriesMediaTree.class, MediaRootType.SERIES);
    StandardView.registerLayout(SerieItem.class, MediaRootType.SERIE_EPISODES);
  }

  public void init() {
    enrichCache.registerEnricher(Identifier.class, new IdentifierEnricher(identifierDao, Activator.TVDB_SERIE_ENRICHER, SerieBase.class));
    enrichCache.registerEnricher(Identifier.class, new IdentifierEnricher(identifierDao, Activator.TVDB_EPISODE_ENRICHER, EpisodeBase.class));
    enrichCache.registerEnricher(Serie.class, serieEnricher);
    enrichCache.registerEnricher(Episode.class, episodeEnricher);
  }

  @Override
  public String getTitle() {
    return "Series";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/media/serie/serie.png"));
  }

  @Override
  public Destination getDestination(final ProgramController controller) {
    return new Destination("serie", getTitle()) {
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
