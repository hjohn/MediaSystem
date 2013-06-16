package hs.mediasystem;

import hs.ddif.Injector;
import hs.ddif.JustInTimeDiscoveryPolicy;
import hs.ddif.PluginManager;
import hs.mediasystem.beans.BeanUtils;
import hs.mediasystem.dao.Setting;
import hs.mediasystem.db.ConnectionPool;
import hs.mediasystem.db.DatabaseStatementTranslator;
import hs.mediasystem.db.DatabaseUpdater;
import hs.mediasystem.db.SimpleConnectionPoolDataSource;
import hs.mediasystem.db.SimpleDatabaseStatementTranslator;
import hs.mediasystem.entity.CachingEntityFactory;
import hs.mediasystem.framework.IdentifierProvider;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.MediaDataPersister;
import hs.mediasystem.framework.MediaDataProvider;
import hs.mediasystem.framework.PersisterProvider;
import hs.mediasystem.framework.Person;
import hs.mediasystem.framework.PersonProvider;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.PlayerFactory;
import hs.mediasystem.screens.AbstractSetting;
import hs.mediasystem.screens.Location;
import hs.mediasystem.screens.LocationHandler;
import hs.mediasystem.screens.MainScreenLocation;
import hs.mediasystem.screens.MainScreenPresentation;
import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.screens.MediaNodeCellProvider;
import hs.mediasystem.screens.MessagePaneTaskExecutor;
import hs.mediasystem.screens.PlaybackLocation;
import hs.mediasystem.screens.PlaybackOverlayPane;
import hs.mediasystem.screens.PlaybackOverlayPresentation;
import hs.mediasystem.screens.Presentation;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.SettingGroup;
import hs.mediasystem.screens.StandardCell;
import hs.mediasystem.screens.collection.CollectionLocation;
import hs.mediasystem.screens.collection.CollectionPresentation;
import hs.mediasystem.screens.collection.CollectionView;
import hs.mediasystem.screens.collection.DetailPaneDecorator;
import hs.mediasystem.screens.collection.DetailPaneDecoratorFactory;
import hs.mediasystem.screens.collection.MediaDetailPaneDecorator;
import hs.mediasystem.screens.collection.PersonDetailPaneDecorator;
import hs.mediasystem.screens.collection.AbstractDetailPane.DecoratablePane;
import hs.mediasystem.screens.optiondialog.BooleanOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.util.DuoWindowSceneManager;
import hs.mediasystem.util.SceneManager;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;

import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.stage.Stage;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.sql.ConnectionPoolDataSource;

public class FrontEnd extends Application {
  private static final Ini INI = new Ini(new File("mediasystem.ini"));

  private SceneManager sceneManager;
  private ConnectionPool pool;
  private DatabaseStatementTranslator translator;

  @Override
  public void start(Stage primaryStage) throws MalformedURLException {
    System.out.println("javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));

    Section generalSection = INI.getSection("general");
    int screenNumber = generalSection == null ? 0 : Integer.parseInt(generalSection.getDefault("screen", "0"));

    sceneManager = new DuoWindowSceneManager("MediaSystem", screenNumber);

    Section databaseIniSection = INI.getSection("database");

    ConnectionPoolDataSource dataSource = databaseIniSection == null ? new SimpleConnectionPoolDataSource("jdbc:derby:db;create=true") : configureDataSource(databaseIniSection);
    String databaseUrl = databaseIniSection == null ? "jdbc:derby:db;create=true" : databaseIniSection.get("url");

    pool = new ConnectionPool(dataSource, 5);
    translator = createDatabaseStatementTranslator(databaseUrl);

    final Injector injector = new Injector(new JustInTimeDiscoveryPolicy());

    injector.registerInstance(injector);

    injector.register(new Provider<Connection>() {
      @Override
      public Connection get() {
        return pool.getConnection();
      }
    });

    PluginManager pluginManager = new PluginManager(injector);

    pluginManager.loadPluginAndScan(new File("../mediasystem-ext-player-vlc/target/mediasystem-ext-player-vlc-1.0.0-SNAPSHOT.jar").toURI().toURL());

    injector.register(new Provider<Player>() {
      @Inject  // TODO can this work?
      private PlayerFactory playerFactory;
      private Player player;

      @Override
      public Player get() {
        if(player == null) {
          Set<PlayerFactory> playerFactories = injector.getInstances(PlayerFactory.class);

          player = playerFactories.iterator().next().create(INI);
        }

        return player;
      }
    });

    injector.register(new Provider<SceneManager>() {
      @Override
      public SceneManager get() {
        return sceneManager;
      }
    });

    injector.register(new Provider<Ini>() {
      @Override
      public Ini get() {
        return INI;
      }
    });

    injector.register(CollectionView.class);  // Implicit

    injector.registerInstance(new IdentifierProvider());
    injector.registerInstance(new MediaDataProvider());
    injector.registerInstance(new PersonProvider());

    injector.register(CachingEntityFactory.class);

    pluginManager.loadPluginAndScan(new File("../mediasystem-ext-all/target/mediasystem-ext-all-1.0.0-SNAPSHOT.jar").toURI().toURL());

    injector.register(new Provider<DatabaseStatementTranslator>() {
      @Override
      public DatabaseStatementTranslator get() {
        return translator;
      }
    });

    DatabaseUpdater updater = injector.getInstance(DatabaseUpdater.class);

    updater.updateDatabase();

    PersisterProvider.register(MediaData.class, injector.getInstance(MediaDataPersister.class));

    injector.registerInstance(new DetailPaneDecoratorFactory() {
      @Override
      public Class<?> getType() {
        return Media.class;
      }

      @Override
      public DetailPaneDecorator<?> create(DecoratablePane decoratablePane) {
        return new MediaDetailPaneDecorator(decoratablePane);
      }
    });

    injector.registerInstance(new DetailPaneDecoratorFactory() {
      @Override
      public Class<?> getType() {
        return Person.class;
      }

      @Override
      public DetailPaneDecorator<?> create(DecoratablePane decoratablePane) {
        return new PersonDetailPaneDecorator(decoratablePane);
      }
    });

    injector.registerInstance(new MediaNodeCellProvider() {
      @Override
      public MediaNodeCell get() {
        return new StandardCell();
      }

      @Override
      public MediaNodeCellProvider.Type getType() {
        return MediaNodeCellProvider.Type.HORIZONTAL;
      }

      @Override
      public Class<?> getMediaType() {
        return Media.class;
      }
    });

    injector.registerInstance(new SettingGroup("video", null, "Video", 0));

    injector.registerInstance(new AbstractSetting("information-bar.debug-mem", null, 0) {
      @Override
      public Option createOption(Set<hs.mediasystem.screens.Setting> settings) {
        final BooleanProperty booleanProperty = injector.getInstance(SettingsStore.class).getBooleanProperty("MediaSystem:InformationBar", Setting.PersistLevel.PERMANENT, "Visible");

        return new BooleanOption("Show Memory Use Information", booleanProperty, new StringBinding(booleanProperty) {
          @Override
          protected String computeValue() {
            return booleanProperty.get() ? "Yes" : "No";
          }
        });
      }
    });

    System.out.println("Creating controller...");

    injector.register(MessagePaneTaskExecutor.class);

    final ProgramController controller = injector.getInstance(ProgramController.class);

    injector.register(PlaybackOverlayPane.class);

    injector.registerInstance(new LocationHandler() {
      @Override
      public Presentation go(Location location, Presentation current) {
        return injector.getInstance(MainScreenPresentation.class);
      }

      @Override
      public Class<? extends Location> getLocationType() {
        return MainScreenLocation.class;
      }
    });

    injector.registerInstance(new LocationHandler() {
      @Override
      public Presentation go(Location location, Presentation current) {
        CollectionPresentation presentation = current instanceof CollectionPresentation ? (CollectionPresentation)current : injector.getInstance(CollectionPresentation.class);

        presentation.setMediaRoot(((CollectionLocation)location).getMediaRoot());

        return presentation;
      }

      @Override
      public Class<? extends Location> getLocationType() {
        return CollectionLocation.class;
      }
    });

    injector.registerInstance(new LocationHandler() {
      @Override
      public Presentation go(Location location, Presentation current) {
        return injector.getInstance(PlaybackOverlayPresentation.class);
      }

      @Override
      public Class<? extends Location> getLocationType() {
        return PlaybackLocation.class;
      }
    });

    injector.getInstance(MessagePaneTaskExecutor.class);  // TODO Initalizes it and registers it with ProgramController, silly way of doing it, fix

    controller.showMainScreen();
  }

  @Override
  public void stop() throws InterruptedException {
    if(pool != null) {
      pool.close();
    }
  }

  private DatabaseStatementTranslator createDatabaseStatementTranslator(String url) {
    String databaseName = url.split(":")[1].toLowerCase();

    if(databaseName.equals("postgresql")) {
      return new SimpleDatabaseStatementTranslator(new HashMap<String, String>() {{
        put("BinaryType", "bytea");
        put("DropNotNull", "DROP NOT NULL");
        put("Sha256Type", "bytea");
        put("SerialType", "serial4");
      }});
    }

    return new SimpleDatabaseStatementTranslator(new HashMap<String, String>() {{
      put("BinaryType", "blob");
      put("DropNotNull", "NULL");
      put("Sha256Type", "char(32) for bit data");
      put("SerialType", "integer generated always as identity");
    }});
  }

  private ConnectionPoolDataSource configureDataSource(Section section)  {
    try {
      Class.forName(section.get("driverClass"));
      Properties properties = new Properties();

      for(String key : section) {
        if(!key.equals("driverClass") && !key.equals("postConnectSql") && !key.equals("url")) {
          properties.put(key, section.get(key));
        }
      }

      SimpleConnectionPoolDataSource dataSource = new SimpleConnectionPoolDataSource(section.get("url"), properties);

      dataSource.setPostConnectSql(section.get("postConnectSql"));

      return dataSource;
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
