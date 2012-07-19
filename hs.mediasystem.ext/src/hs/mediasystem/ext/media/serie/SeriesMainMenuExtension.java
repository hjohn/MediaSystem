package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.IdentifierEnricher;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.Navigator.Destination;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentation;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.screens.selectmedia.StandardView;
import hs.mediasystem.util.PathStringConverter;

import java.nio.file.Path;

import javafx.collections.ObservableList;
import javafx.scene.image.Image;

public class SeriesMainMenuExtension implements MainMenuExtension {
  private volatile SelectMediaPresentationProvider selectMediaPresentationProvider;
  private volatile SerieEnricher serieEnricher;
  private volatile EpisodeEnricher episodeEnricher;
  private volatile EnrichCache enrichCache;
  private volatile PersistQueue persister;
  private volatile IdentifierDao identifierDao;
  private volatile SettingsStore settingsStore;

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
  public void select(final ProgramController controller) {
    controller.getNavigator().navigateTo(new Destination("serie", getTitle()) {
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
          final ObservableList<Path> paths = settingsStore.getListProperty("MediaSystem:Ext:Series", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

          mediaTree = new SeriesMediaTree(enrichCache, persister, paths);
          presentation.setMediaTree(mediaTree);
        }
      }
    });
  }

  @Override
  public double order() {
    return 0.2;
  }
}
