package hs.mediasystem;

import hs.mediasystem.db.ConnectionPool;
import hs.mediasystem.db.DatabaseUpdater;
import hs.mediasystem.ext.movie.MoviesMainMenuExtension;
import hs.mediasystem.ext.serie.SeriesMainMenuExtension;
import hs.mediasystem.framework.PlaybackOverlayView;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MessagePaneExecutionQueue;
import hs.mediasystem.screens.PlaybackOverlayPane;
import hs.mediasystem.screens.PlayerPresentation;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.SelectMediaView;
import hs.mediasystem.screens.selectmedia.BannerStandardLayoutExtension;
import hs.mediasystem.screens.selectmedia.ListStandardLayoutExtension;
import hs.mediasystem.screens.selectmedia.NosMainMenuExtension;
import hs.mediasystem.screens.selectmedia.StandardLayoutExtension;
import hs.mediasystem.screens.selectmedia.StandardView;
import hs.mediasystem.screens.selectmedia.YouTubeMainMenuExtension;
import hs.mediasystem.util.DuoWindowSceneManager;
import hs.mediasystem.util.ExecutionQueue;
import hs.mediasystem.util.SceneManager;
import hs.mediasystem.util.StateCache;
import hs.mediasystem.util.TaskExecutor;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.stage.Stage;
import net.sf.jtmdb.GeneralSettings;

import org.postgresql.ds.PGConnectionPoolDataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

public class FrontEnd extends Application {
  private static final Ini INI = new Ini(new File("mediasystem.ini"));

  private final StateCache stateCache = new StateCache();

  private SceneManager sceneManager;
  private Player player;
  private ConnectionPool pool;

  @Override
  public void init() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    Section section = INI.getSection("general");

    GeneralSettings.setApiKey(section.get("jtmdb.key"));
    GeneralSettings.setLogEnabled(false);
    GeneralSettings.setLogStream(System.out);

    String factoryClassName = section.getDefault("player.factoryClass", "hs.mediasystem.players.vlc.VLCPlayerFactory");

    PlayerFactory playerFactory = (PlayerFactory)Class.forName(factoryClassName).newInstance();
    player = playerFactory.create(INI);
  }

  @Override
  public void start(Stage primaryStage) {
    System.out.println("javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));

    int screenNumber = Integer.parseInt(INI.getSection("general").getDefault("screen", "0"));

    sceneManager = new DuoWindowSceneManager("MediaSystem", screenNumber);

    PGConnectionPoolDataSource dataSource = new PGConnectionPoolDataSource();

    Section db = INI.getSection("database");

    dataSource.setServerName(db.getDefault("host", "127.0.0.1"));
    dataSource.setPortNumber(Integer.parseInt(db.getDefault("port", "5432")));
    dataSource.setDatabaseName(db.getDefault("name", "mediasystem"));
    dataSource.setPassword(db.get("password"));
    dataSource.setUser(db.get("user"));

    pool = new ConnectionPool(dataSource, 5);

    Module module = new AbstractModule() {
      private final PlayerPresentation playerPresentation = new PlayerPresentation(player);

      @Override
      protected void configure() {
        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(MoviesMainMenuExtension.class);
        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(SeriesMainMenuExtension.class);
        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(YouTubeMainMenuExtension.class);
        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(NosMainMenuExtension.class);

        Multibinder.newSetBinder(binder(), StandardLayoutExtension.class).addBinding().to(ListStandardLayoutExtension.class);
        Multibinder.newSetBinder(binder(), StandardLayoutExtension.class).addBinding().to(BannerStandardLayoutExtension.class);

        bind(SelectMediaView.class).to(StandardView.class);
        bind(PlaybackOverlayView.class).to(PlaybackOverlayPane.class);
        bind(ExecutionQueue.class).to(MessagePaneExecutionQueue.class);
        bind(TaskExecutor.class).to(MessagePaneExecutionQueue.class);
      }

      @Provides
      public StateCache providesStateCache() {
        return stateCache;
      }

      @Provides
      public PlayerPresentation providesPlayerPresentation() {
        return playerPresentation;
      }

      @Provides
      public SceneManager providesSceneManager() {
        return sceneManager;
      }

      @Provides
      public Ini providesIni() {
        return INI;
      }

      @Provides
      public Connection providesConnection() {
        try {
          Connection connection = pool.getConnection();

          try(PreparedStatement statement = connection.prepareStatement("SET search_path = public")) {
            statement.execute();
          }

          return connection;
        }
        catch(SQLException e) {
          throw new RuntimeException(e);
        }
      }
    };

    Injector injector = Guice.createInjector(module);

    DatabaseUpdater updater = injector.getInstance(DatabaseUpdater.class);

    updater.updateDatabase();

    ProgramController controller = injector.getInstance(ProgramController.class);

    controller.showMainScreen();
  }

  @Override
  public void stop() {
    if(pool != null) {
      pool.close();
    }
  }
}
