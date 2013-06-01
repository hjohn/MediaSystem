package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.entity.EntityFactory;
import hs.mediasystem.framework.MediaItemConfigurator;
import hs.mediasystem.framework.MediaRootType;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.SelectMediaLocation;
import hs.mediasystem.screens.selectmedia.StandardView;
import hs.mediasystem.util.PathStringConverter;

import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.collections.ObservableList;
import javafx.scene.image.Image;

@Named
public class MoviesMainMenuExtension implements MainMenuExtension {
  @Inject
  private PersistQueue persister;
  @Inject
  private MediaItemConfigurator mediaItemConfigurator;
  @Inject
  private ItemsDao itemsDao;
  @Inject
  private EntityFactory<DatabaseObject> entityFactory;
  @Inject
  private SettingsStore settingsStore;

  public MoviesMainMenuExtension() {
    StandardView.registerLayout(MoviesMediaTree.class, MediaRootType.MOVIES);
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

    controller.setLocation(new SelectMediaLocation(new MoviesMediaTree(persister, itemsDao, mediaItemConfigurator, entityFactory, paths)));
  }

  @Override
  public double order() {
    return 0.1;
  }
}
