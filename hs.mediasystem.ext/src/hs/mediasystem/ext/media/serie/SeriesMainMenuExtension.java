package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.EntityFactory;
import hs.mediasystem.framework.MediaItemConfigurator;
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
  private volatile PersistQueue persister;
  private volatile ItemsDao itemsDao;
  private volatile MediaItemConfigurator mediaItemConfigurator;
  private volatile EntityFactory entityFactory;
  private volatile SettingsStore settingsStore;

  public SeriesMainMenuExtension() {
    StandardView.registerLayout(SeriesMediaTree.class, MediaRootType.SERIES);
    StandardView.registerLayout(SerieItem.class, MediaRootType.SERIE_EPISODES);
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

    controller.setLocation(new SelectMediaLocation(new SeriesMediaTree(persister, itemsDao, mediaItemConfigurator, entityFactory, paths)));
  }

  @Override
  public double order() {
    return 0.2;
  }
}
