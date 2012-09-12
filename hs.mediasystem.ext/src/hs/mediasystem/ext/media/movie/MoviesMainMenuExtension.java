package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaDataDao;
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

public class MoviesMainMenuExtension implements MainMenuExtension {
  private volatile EnrichCache enrichCache;
  private volatile PersistQueue persister;
  private volatile MediaDataDao mediaDataDao;
  private volatile IdentifierDao identifierDao;
  private volatile ItemsDao itemsDao;
  private volatile EntityFactory entityFactory;
  private volatile SettingsStore settingsStore;

  public MoviesMainMenuExtension() {
    StandardView.registerLayout(MoviesMediaTree.class, MediaRootType.MOVIES);
  }

  public void init() {
    enrichCache.registerEnricher(Identifier.class, new IdentifierEnricher(identifierDao, Activator.TMDB_ENRICHER, Movie.class));
  }

  @Override
  public String getTitle() {
    return "Movies";
  }

  @Override
  public Image getImage() {
    return new Image(getClass().getResourceAsStream("/hs/mediasystem/ext/media/movie/movie.png"));
  }

  @Override
  public void select(final ProgramController controller) {
    ObservableList<Path> paths = settingsStore.getListProperty("MediaSystem:Ext:Movies", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

    controller.setLocation(new SelectMediaLocation(new MoviesMediaTree(enrichCache, persister, itemsDao, mediaDataDao, identifierDao, entityFactory, paths)));
  }

  @Override
  public double order() {
    return 0.1;
  }
}
