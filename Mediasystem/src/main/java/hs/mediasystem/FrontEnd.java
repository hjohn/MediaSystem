package hs.mediasystem;

import hs.mediasystem.beans.BeanUtils;
import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.db.ConnectionPool;
import hs.mediasystem.db.DatabaseUpdater;
import hs.mediasystem.db.SimpleConnectionPoolDataSource;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaDataEnricher;
import hs.mediasystem.framework.MediaDataPersister;
import hs.mediasystem.framework.PersisterProvider;
import hs.mediasystem.framework.PlaybackOverlayView;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.player.PlayerFactory;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.AbstractSetting;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.screens.MediaNodeCellProvider;
import hs.mediasystem.screens.MessagePaneTaskExecutor;
import hs.mediasystem.screens.PlaybackOverlayPane;
import hs.mediasystem.screens.PlayerPresentation;
import hs.mediasystem.screens.PluginTracker;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.Setting;
import hs.mediasystem.screens.StandardCell;
import hs.mediasystem.screens.optiondialog.BooleanOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.selectmedia.DetailPane;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.screens.selectmedia.SelectMediaView;
import hs.mediasystem.screens.selectmedia.StandardDetailPane;
import hs.mediasystem.screens.selectmedia.StandardView;
import hs.mediasystem.util.DuoWindowSceneManager;
import hs.mediasystem.util.SceneManager;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.TaskExecutor;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
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
import java.util.Properties;
import java.util.ServiceLoader;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.stage.Stage;

import javax.sql.ConnectionPoolDataSource;

import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

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

    ConnectionPoolDataSource dataSource = configureDataSource(INI.getSection("database"));

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

          if(factory != null) {
            try {
              playerPresentation = new PlayerPresentation(factory.create(INI));
            }
            catch(Exception e) {
              System.out.println("[SEVERE] Could not configure a Video Player: " + e);
            }
          }
          else {
            System.out.println("[SEVERE] Could not configure a Video Player, no PlayerFactory found.");
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

    DatabaseUpdater updater = injector.getInstance(DatabaseUpdater.class);

    updater.updateDatabase();

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
      .setInterface(IdentifierDao.class.getName(), null)
      .setImplementation(injector.getInstance(IdentifierDao.class))
    );

    dm.add(dm.createComponent()
      .setInterface(SettingsStore.class.getName(), null)
      .setImplementation(injector.getInstance(SettingsStore.class))
    );

    dm.add(dm.createComponent()
      .setInterface(MediaNodeCellProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Media.class);
        put("type", MediaNodeCellProvider.Type.HORIZONTAL);
      }})
      .setImplementation(new MediaNodeCellProvider() {
        @Override
        public MediaNodeCell get() {
          return new StandardCell();
        }
      })
    );

    dm.add(dm.createComponent()
      .setInterface(Setting.class.getName(), null)
      .setImplementation(new AbstractSetting("information-bar.debug-mem", 0) {
        private volatile SettingsStore settingsStore;

        @Override
        public Option createOption() {
          final BooleanProperty booleanProperty = settingsStore.getBooleanProperty("MediaSystem:InformationBar", PersistLevel.PERMANENT, "Visible");

          return new BooleanOption("Show Memory Use Information", booleanProperty, new StringBinding(booleanProperty) {
            @Override
            protected String computeValue() {
              return booleanProperty.get() ? "Yes" : "No";
            }
          });
        }
      })
      .add(dm.createServiceDependency()
        .setService(SettingsStore.class)
        .setRequired(true)
      )
    );

    injector.getInstance(EnrichCache.class).registerEnricher(MediaData.class, injector.getInstance(MediaDataEnricher.class));

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

  private ConnectionPoolDataSource configureDataSource(Section section)  {
    try {
      Class.forName(section.get("driverClass"));
      Properties properties = new Properties();

      for(String key : section) {
        if(!key.equals("driverClass") && !key.equals("url")) {
          properties.put(key, section.get(key));
        }
      }

      return new SimpleConnectionPoolDataSource(section.get("url"), properties);
    }
    catch(ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("unused")
  private ConnectionPoolDataSource configureDataSourceAdvanced(Section section)  {
    try {
      String dataSourceClassName = section.get("dataSourceClass");
      Class<?> dataSourceClass = Class.forName(dataSourceClassName);
      Lookup lookup = MethodHandles.lookup();

      MethodHandle constructor = lookup.findConstructor(dataSourceClass, MethodType.methodType(void.class));

      @SuppressWarnings("cast")
      ConnectionPoolDataSource dataSource = (ConnectionPoolDataSource)constructor.invoke();

      for(String key : section) {
        if(!key.equals("dataSourceClass")) {
          Method setter = BeanUtils.getSetter(dataSourceClass, key);
          String value = section.get(key);
          Object parameter = value;
          Class<?> argumentType = setter.getParameterTypes()[0];

          if(argumentType == Integer.class || argumentType == int.class) {
            parameter = Integer.parseInt(value);
          }

          try {
            setter.invoke(dataSource, parameter);
          }
          catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("expected: " + argumentType, e);
          }
        }
      }

      return dataSource;
    }
    catch(Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
