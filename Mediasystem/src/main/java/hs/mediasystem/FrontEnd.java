package hs.mediasystem;

import hs.mediasystem.db.ConnectionPool;
import hs.mediasystem.db.DatabaseUpdater;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.MediaDataEnricher;
import hs.mediasystem.framework.PlaybackOverlayView;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.persist.Persister;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MessagePaneTaskExecutor;
import hs.mediasystem.screens.PlaybackOverlayPane;
import hs.mediasystem.screens.PlayerPresentation;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.SelectMediaView;
import hs.mediasystem.screens.selectmedia.BannerStandardLayoutExtension;
import hs.mediasystem.screens.selectmedia.ListStandardLayoutExtension;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.screens.selectmedia.StandardLayoutExtension;
import hs.mediasystem.screens.selectmedia.StandardView;
import hs.mediasystem.util.DuoWindowSceneManager;
import hs.mediasystem.util.SceneManager;
import hs.mediasystem.util.StateCache;
import hs.mediasystem.util.TaskExecutor;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.ServiceLoader;

import javafx.application.Application;
import javafx.stage.Stage;
import net.sf.jtmdb.GeneralSettings;

import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.postgresql.ds.PGConnectionPoolDataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
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
  public void start(Stage primaryStage) throws InterruptedException {
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

    final Framework framework = createOSGI();

//  dm.add(dm.createComponent().setImplementation(this).add(dm.createServiceDependency().setService(MainMenuExtension.class).setCallbacks("add", "remove")));

    Module module = new AbstractModule() {
      private final PlayerPresentation playerPresentation = new PlayerPresentation(player);
      PluginTracker<MainMenuExtension> mainMenuExtensions = new PluginTracker<>(framework.getBundleContext(), MainMenuExtension.class);

      @Override
      protected void configure() {
//        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(MoviesMainMenuExtension.class);
//        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(SeriesMainMenuExtension.class);
//        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(YouTubeMainMenuExtension.class);
//        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(NosMainMenuExtension.class);

        Multibinder.newSetBinder(binder(), StandardLayoutExtension.class).addBinding().to(ListStandardLayoutExtension.class);
        Multibinder.newSetBinder(binder(), StandardLayoutExtension.class).addBinding().to(BannerStandardLayoutExtension.class);

        bind(SelectMediaView.class).to(StandardView.class);
        bind(PlaybackOverlayView.class).to(PlaybackOverlayPane.class);
        bind(TaskExecutor.class).to(MessagePaneTaskExecutor.class);


      //  bind(TypeLiterals.iterable(MainMenuExtension.class)).toProvider(Peaberry.service(MainMenuExtension.class).multiple());

        bind(new TypeLiteral<PluginTracker<MainMenuExtension>>() {}).toProvider(new Provider<PluginTracker<MainMenuExtension>>() {
          @Override
          public PluginTracker<MainMenuExtension> get() {
            return mainMenuExtensions;
          }
        });


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
      public BundleContext providesBundleContext() {
        return framework.getBundleContext();
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


//    Thread.sleep(2500);

    Injector injector = Guice.createInjector(
      //Peaberry.osgiModule(framework.getBundleContext()),
      module);

//    Thread.sleep(2500);

    DependencyManager dm = new DependencyManager(framework.getBundleContext());

    dm.add(dm.createComponent()
      .setInterface(SelectMediaPresentationProvider.class.getName(), null)
      .setImplementation(injector.getInstance(SelectMediaPresentationProvider.class))
    );

    dm.add(dm.createComponent()
      .setInterface(EnrichCache.class.getName(), null)
      .setImplementation(injector.getInstance(EnrichCache.class))
    );

    dm.add(dm.createComponent()
      .setInterface(Persister.class.getName(), null)
      .setImplementation(injector.getInstance(Persister.class))
    );

    dm.add(dm.createComponent()
      .setInterface(ItemsDao.class.getName(), null)
      .setImplementation(injector.getInstance(ItemsDao.class))
    );

    dm.add(dm.createComponent()
      .setInterface(TypeBasedItemEnricher.class.getName(), null)
      .setImplementation(injector.getInstance(TypeBasedItemEnricher.class))
    );

    DatabaseUpdater updater = injector.getInstance(DatabaseUpdater.class);

    updater.updateDatabase();

    injector.getInstance(EnrichCache.class).registerEnricher(MediaData.class, injector.getInstance(MediaDataEnricher.class));

    ProgramController controller = injector.getInstance(ProgramController.class);

    controller.showMainScreen();

    //framework.waitForStop(0);
    //System.exit(0);
  }

  @Override
  public void stop() {
    if(pool != null) {
      pool.close();
    }
  }


  private static Framework framework = null;

  private static Framework createOSGI() {
    String[] locations = new String[] {
      "file:org.apache.felix.shell-1.5.0-SNAPSHOT.jar",
      "file:org.apache.felix.shell.tui-1.5.0-SNAPSHOT.jar",
      "file:../hs.mediasystem.ext/generated/hs.mediasystem.ext.movie.jar",
      "file:../hs.mediasystem.ext/generated/hs.mediasystem.ext.nos.jar",
      "file:../hs.mediasystem.ext/generated/hs.mediasystem.ext.shutdown.jar",
      "file:../hs.mediasystem.ext/generated/hs.mediasystem.ext.youtube.jar"
    };

    try {
      Map<String, String> config = ConfigUtil.createConfig();
      framework = createFramework(config);
      framework.init();
      framework.start();
      installAndStartBundles(locations);

      return framework;
    }
    catch(Exception e) {
      System.err.println("Could not create framework: " + e);
      e.printStackTrace();
      System.exit(-1);
      return null;
    }
  }

  private static Framework createFramework(Map<String, String> config) {
    ServiceLoader<FrameworkFactory> factoryLoader = ServiceLoader.load(FrameworkFactory.class);

    for(FrameworkFactory factory : factoryLoader){
      return factory.newFramework(config);
    }

    throw new IllegalStateException("Unable to load FrameworkFactory service.");
  }

  private static void installAndStartBundles(String... bundleLocations) throws BundleException {
    BundleContext bundleContext = framework.getBundleContext();
//    Activator hostActivator = new Activator();
//    hostActivator.start(bundleContext);
    for (String location : bundleLocations) {
      Bundle addition = bundleContext.installBundle(location);
      addition.start();
    }
  }
}
