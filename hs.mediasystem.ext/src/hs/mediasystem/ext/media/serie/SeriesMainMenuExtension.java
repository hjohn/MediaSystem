package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.EntityFactory;
import hs.mediasystem.framework.IdentifierEnricher;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.fs.MediaRootType;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.SelectMediaLocation;
import hs.mediasystem.screens.selectmedia.StandardView;
import hs.mediasystem.util.PathStringConverter;

import java.nio.file.Path;

import javafx.collections.ObservableList;
import javafx.scene.image.Image;

public class SeriesMainMenuExtension implements MainMenuExtension {
  private volatile EnrichCache enrichCache;
  private volatile PersistQueue persister;
  private volatile IdentifierDao identifierDao;
  private volatile ItemsDao itemsDao;
  private volatile EntityFactory entityFactory;
  private volatile SettingsStore settingsStore;

  public SeriesMainMenuExtension() {
    StandardView.registerLayout(SeriesMediaTree.class, MediaRootType.SERIES);
    StandardView.registerLayout(SerieItem.class, MediaRootType.SERIE_EPISODES);
  }

  public void init() {
    enrichCache.registerEnricher(Identifier.class, new IdentifierEnricher(identifierDao, Activator.TVDB_SERIE_ENRICHER, Serie.class));
    enrichCache.registerEnricher(Identifier.class, new IdentifierEnricher(identifierDao, Activator.TVDB_EPISODE_ENRICHER, Episode.class));
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
    ObservableList<Path> paths = settingsStore.getListProperty("MediaSystem:Ext:Series", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

    controller.setLocation(new SelectMediaLocation(new SeriesMediaTree(enrichCache, persister, itemsDao, entityFactory, paths)));
  }

  @Override
  public double order() {
    return 0.2;
  }
}
