package hs.mediasystem;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.db.ConnectionPool;
import hs.mediasystem.db.DatabaseUpdater;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.IdentifierEnricher;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaDataEnricher;
import hs.mediasystem.framework.MediaDataPersister;
import hs.mediasystem.framework.PersisterProvider;
import hs.mediasystem.framework.PlaybackOverlayView;
import hs.mediasystem.framework.TypeBasedItemEnricher;
import hs.mediasystem.framework.player.PlayerFactory;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MessagePaneTaskExecutor;
import hs.mediasystem.screens.PlaybackOverlayPane;
import hs.mediasystem.screens.PlayerPresentation;
import hs.mediasystem.screens.PluginTracker;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.selectmedia.DetailPane;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.screens.selectmedia.SelectMediaView;
import hs.mediasystem.screens.selectmedia.StandardDetailPane;
import hs.mediasystem.screens.selectmedia.StandardView;
import hs.mediasystem.util.DuoWindowSceneManager;
import hs.mediasystem.util.SceneManager;
import hs.mediasystem.util.TaskExecutor;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;
import java.util.ServiceLoader;

import javafx.application.Application;
import javafx.stage.Stage;

import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
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

public class FrontEnd extends Application {
  private static final Ini INI = new Ini(new File("mediasystem.ini"));

  private SceneManager sceneManager;
  private ConnectionPool pool;
  private Framework framework;

  @Override
  public void start(Stage primaryStage) throws BundleException {
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

    framework = createHostedOSGiEnvironment();

    Module module = new AbstractModule() {
      private PlayerPresentation playerPresentation;
      PluginTracker<MainMenuExtension> mainMenuExtensions = new PluginTracker<>(framework.getBundleContext(), MainMenuExtension.class);

      @Override
      protected void configure() {
        bind(SelectMediaView.class).to(StandardView.class);
        bind(PlaybackOverlayView.class).to(PlaybackOverlayPane.class);
        bind(TaskExecutor.class).to(MessagePaneTaskExecutor.class);

        bind(new TypeLiteral<PluginTracker<MainMenuExtension>>() {}).toProvider(new Provider<PluginTracker<MainMenuExtension>>() {
          @Override
          public PluginTracker<MainMenuExtension> get() {
            return mainMenuExtensions;
          }
        });
      }

      @Provides
      public PlayerPresentation providesPlayerPresentation() {
        if(playerPresentation == null) {
          ServiceReference<PlayerFactory> serviceReference = framework.getBundleContext().getServiceReference(PlayerFactory.class);
          PlayerFactory factory = framework.getBundleContext().getService(serviceReference);

          try {
            playerPresentation = new PlayerPresentation(factory.create(INI));
          }
          catch(Exception e) {
            System.out.println("[SEVERE] Could not configure a Video Player!");
            System.out.println("[SEVERE] " + e.getMessage());
          }
        }

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

    Injector injector = Guice.createInjector(module);

    PersisterProvider.register(MediaData.class, injector.getInstance(MediaDataPersister.class));

    DependencyManager dm = new DependencyManager(framework.getBundleContext());

    dm.add(dm.createComponent()
      .setInterface(DetailPane.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Media.class);
      }})
      .setImplementation(StandardDetailPane.class)
    );

    dm.add(dm.createComponent()
      .setInterface(SelectMediaPresentationProvider.class.getName(), null)
      .setImplementation(injector.getInstance(SelectMediaPresentationProvider.class))
    );

    dm.add(dm.createComponent()
      .setInterface(EnrichCache.class.getName(), null)
      .setImplementation(injector.getInstance(EnrichCache.class))
    );

    dm.add(dm.createComponent()
      .setInterface(PersistQueue.class.getName(), null)
      .setImplementation(injector.getInstance(PersistQueue.class))
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
    injector.getInstance(EnrichCache.class).registerEnricher(Identifier.class, injector.getInstance(IdentifierEnricher.class));

    ProgramController controller = injector.getInstance(ProgramController.class);

    controller.showMainScreen();
  }

  @Override
  public void stop() throws InterruptedException {
    if(framework != null) {
      framework.waitForStop(5000);
    }
    if(pool != null) {
      pool.close();
    }
  }

  private Framework createHostedOSGiEnvironment() throws BundleException {
    Map<String, String> config = ConfigUtil.createConfig();
    Framework framework = createFramework(config);
    framework.init();
    framework.start();

    monitorBundles(
      Paths.get("local/bundles"),
      Paths.get("../cnf/repo/com.springsource.org.apache.log4j"),
      Paths.get("../cnf/repo/jackson-core-lgpl"),
      Paths.get("../cnf/repo/jackson-mapper-lgpl"),
      Paths.get("../hs.mediasystem.ext/generated")
    );

    return framework;
  }

  private void monitorBundles(final Path... monitorPaths) {
    new Thread() {
      {
        setDaemon(true);
      }

      @Override
      public void run() {
        try {
          WatchService watchService = FileSystems.getDefault().newWatchService();

          for(Path path : monitorPaths) {
            try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(path, "*.jar")) {
              for(Path newPath : dirStream) {
                installAndStartBundle(newPath);
              }
            }

            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
          }

          for(;;) {
            WatchKey key = watchService.take();

            for(WatchEvent<?> event : key.pollEvents()) {
              Path newPath = (Path)event.context();

              if(newPath.getFileName().toString().endsWith(".jar")) {
                installAndStartBundle(newPath);
              }
            }

            key.reset();
          }
        }
        catch(IOException | InterruptedException e) {
          e.printStackTrace();
        }
      }

      private void installAndStartBundle(Path path) {
        BundleContext bundleContext = framework.getBundleContext();

        try {
          Bundle bundle = bundleContext.installBundle(path.toUri().toString());
          bundle.start();
        }
        catch(BundleException e) {
          System.out.println("[WARN] Bundle Hot Deploy Monitor - Exception while installing bundle '" + path.toUri() + "': " + e);
          e.printStackTrace(System.out);
        }
      }

    }.start();
  }

  private static Framework createFramework(Map<String, String> config) {
    ServiceLoader<FrameworkFactory> factoryLoader = ServiceLoader.load(FrameworkFactory.class);

    for(FrameworkFactory factory : factoryLoader){
      return factory.newFramework(config);
    }

    throw new IllegalStateException("Unable to load FrameworkFactory service.");
  }
}
