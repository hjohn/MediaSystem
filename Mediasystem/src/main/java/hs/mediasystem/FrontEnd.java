package hs.mediasystem;

import hs.ddif.AnnotationDescriptor;
import hs.ddif.Injector;
import hs.ddif.JustInTimeDiscoveryPolicy;
import hs.ddif.PluginManager;
import hs.ddif.Value;
import hs.mediasystem.beans.BeanUtils;
import hs.mediasystem.dao.Setting;
import hs.mediasystem.db.ConnectionPool;
import hs.mediasystem.db.DatabaseStatementTranslator;
import hs.mediasystem.db.DatabaseUpdater;
import hs.mediasystem.db.SimpleConnectionPoolDataSource;
import hs.mediasystem.db.SimpleDatabaseStatementTranslator;
import hs.mediasystem.entity.Entity;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityEnricher;
import hs.mediasystem.entity.EntityListProvider;
import hs.mediasystem.entity.EntityPersister;
import hs.mediasystem.framework.DatabaseCache;
import hs.mediasystem.framework.MediaDataPersister;
import hs.mediasystem.framework.MediaEnricher;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.PlayerFactory;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.AbstractSetting;
import hs.mediasystem.screens.MessagePaneTaskExecutor;
import hs.mediasystem.screens.ProgramController;
import hs.mediasystem.screens.SettingGroup;
import hs.mediasystem.screens.collection.CollectionLayout;
import hs.mediasystem.screens.collection.detail.MediaLayout;
import hs.mediasystem.screens.collection.detail.PersonLayout;
import hs.mediasystem.screens.main.MainScreenLayout;
import hs.mediasystem.screens.optiondialog.BooleanOption;
import hs.mediasystem.screens.optiondialog.ListOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.playback.PlaybackLayout;
import hs.mediasystem.screens.playback.PlaybackOverlayPane;
import hs.mediasystem.util.DuoWindowSceneManager;
import hs.mediasystem.util.SceneManager;
import hs.mediasystem.util.StringBinding;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import javax.inject.Named;
import javax.inject.Provider;
import javax.sql.ConnectionPoolDataSource;

public class FrontEnd extends Application {
  private static final Logger LOGGER = Logger.getLogger(FrontEnd.class.getName());
  private static final Comparator<PlayerFactory> PLAYER_FACTORY_COMPARATOR = new Comparator<PlayerFactory>() {
    @Override
    public int compare(PlayerFactory o1, PlayerFactory o2) {
      return o1.getName().compareTo(o2.getName());
    }
  };

  private static final Ini INI = new Ini(new File("mediasystem.ini"));

  private SceneManager sceneManager;
  private ConnectionPool pool;
  private DatabaseStatementTranslator translator;

  @Override
  public void start(Stage primaryStage) throws IOException {
    JavaFXDefaults.setup();

    LOGGER.info("javafx.runtime.version: " + System.getProperties().get("javafx.runtime.version"));
    LOGGER.info("Platform.isSupported(ConditionalFeature.TWO_LEVEL_FOCUS): " + Platform.isSupported(ConditionalFeature.TWO_LEVEL_FOCUS));

    setUserAgentStylesheet(STYLESHEET_CASPIAN);

    Section generalSection = INI.getSection("general");
    int screenNumber = generalSection == null ? 0 : Integer.parseInt(generalSection.getDefault("screen", "0"));

    sceneManager = new DuoWindowSceneManager("MediaSystem", screenNumber);

    final Injector injector = new Injector(new JustInTimeDiscoveryPolicy());

    injector.registerInstance(injector);
    injector.registerInstance(7 * 24 * 60 * 60, AnnotationDescriptor.describe(Named.class, new Value("value", "TheMovieDatabase.expirationSeconds")));

    configureDatabase(injector);

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

    injector.register(MediaEnricher.class);
    injector.register(MediaDataPersister.class);

    injector.register(DatabaseCache.class);

    EntityContext context = new EntityContext(new PersistQueue(3000));

    injector.registerInstance(context);

    loadPlugins(injector);

    ObjectProperty<PlayerFactory> selectedPlayerFactory = configurePlayers(injector);

    configureEntityContext(context, injector);

    injector.registerInstance(new MediaLayout());
    injector.registerInstance(new PersonLayout());

    configureSettings(injector, selectedPlayerFactory);

    LOGGER.fine("Creating controller...");

    injector.register(MessagePaneTaskExecutor.class);

    final ProgramController controller = injector.getInstance(ProgramController.class);

    injector.register(PlaybackOverlayPane.class);

    injector.register(PlaybackLayout.class);
    injector.register(MainScreenLayout.class);
    injector.register(CollectionLayout.class);

    injector.getInstance(MessagePaneTaskExecutor.class);  // TODO Initalizes it and registers it with ProgramController, silly way of doing it, fix

    controller.showMainScreen();
  }

  private void loadPlugins(final Injector injector) throws IOException {
    PluginManager pluginManager = new PluginManager(injector);

    Path pluginsPath = Paths.get("plugins");

    if(Files.isDirectory(pluginsPath)) {
      Files.find(pluginsPath, 10, (p, bfa) -> bfa.isRegularFile()).forEach(p -> {
        try {
          LOGGER.info("Loading plugin: " + p);
          pluginManager.loadPluginAndScan(p.toUri().toURL());
        }
        catch(Exception e) {
          throw new IllegalStateException(e);
        }
      });
    }
    else {
      pluginManager.loadPluginAndScan(new File("../mediasystem-ext-player-vlc/target/mediasystem-ext-player-vlc-1.0.0-SNAPSHOT.jar").toURI().toURL());
      pluginManager.loadPluginAndScan(new File("../mediasystem-ext-all/target/mediasystem-ext-all-1.0.0-SNAPSHOT.jar").toURI().toURL());
    }
  }

  private void configureSettings(final Injector injector, ObjectProperty<PlayerFactory> selectedPlayerFactory) {
    injector.registerInstance(new SettingGroup("video", null, "Video", 0));

    injector.registerInstance(new AbstractSetting("video.player", "video", 0) {
      @Override
      public Option createOption(Set<hs.mediasystem.screens.Setting> settings) {
        ObservableList<PlayerFactory> playerFactories = FXCollections.observableArrayList(injector.getInstances(PlayerFactory.class));

        Collections.sort(playerFactories, PLAYER_FACTORY_COMPARATOR);

        return new ListOption<>("Player", selectedPlayerFactory, playerFactories, new StringBinding(selectedPlayerFactory) {
          @Override
          protected String computeValue() {
            return selectedPlayerFactory.get().getName();
          }
        });
      }
    });

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
  }

  private ObjectProperty<PlayerFactory> configurePlayers(final Injector injector) {
    ObjectProperty<PlayerFactory> selectedPlayerFactory = injector.getInstance(SettingsStore.class).getProperty("MediaSystem:Video", Setting.PersistLevel.PERMANENT, "Player", new StringConverter<PlayerFactory>() {
      @Override
      public PlayerFactory fromString(String text) {
        List<PlayerFactory> instances = new ArrayList<>(injector.getInstances(PlayerFactory.class));

        for(PlayerFactory playerFactory : instances) {
          if(playerFactory.getClass().getName().equals(text)) {
            return playerFactory;
          }
        }

        Collections.sort(instances, PLAYER_FACTORY_COMPARATOR);

        return instances.get(0);
      }

      @Override
      public String toString(PlayerFactory factory) {
        return factory.getClass().getName();
      }
    });

    injector.register(new Provider<Player>() {
      private Player player;

      @Override
      public Player get() {
        if(player == null) {
          player = selectedPlayerFactory.get().create(INI);
        }

        return player;
      }
    });
    return selectedPlayerFactory;
  }

  private void configureDatabase(Injector injector) {
    Section databaseIniSection = INI.getSection("database");

    try {
      Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
    }
    catch(ClassNotFoundException e) {
      throw new IllegalStateException(e);
    }

    ConnectionPoolDataSource dataSource = databaseIniSection == null ? new SimpleConnectionPoolDataSource("jdbc:derby:db;create=true") : configureDataSource(databaseIniSection);
    String databaseUrl = databaseIniSection == null ? "jdbc:derby:db;create=true" : databaseIniSection.get("url");

    pool = new ConnectionPool(dataSource, 5);
    translator = createDatabaseStatementTranslator(databaseUrl);

    injector.register(new Provider<Connection>() {
      @Override
      public Connection get() {
        return pool.getConnection();
      }
    });

    injector.register(new Provider<DatabaseStatementTranslator>() {
      @Override
      public DatabaseStatementTranslator get() {
        return translator;
      }
    });

    DatabaseUpdater updater = injector.getInstance(DatabaseUpdater.class);

    updater.updateDatabase();
  }

  @Override
  public void stop() throws InterruptedException {
    if(pool != null) {
      pool.close();
    }
  }

  @SuppressWarnings("unchecked")
  private void configureEntityContext(EntityContext context, Injector injector) {
    for(hs.mediasystem.entity.Enricher<Entity, Object> enricher : injector.getInstances(hs.mediasystem.entity.Enricher.class)) {
      EntityEnricher entityEnricher = enricher.getClass().getAnnotation(EntityEnricher.class);
      context.registerEnricher((Class<Entity>)entityEnricher.entityClass(), injector.getInstance(entityEnricher.sourceClass()), entityEnricher.priority(), enricher);
    }

    for(hs.mediasystem.entity.ListProvider<Entity, ?> provider : injector.getInstances(hs.mediasystem.entity.ListProvider.class)) {
      EntityListProvider entityListProvider = provider.getClass().getAnnotation(EntityListProvider.class);
      context.registerListProvider((Class<Entity>)entityListProvider.parentEntityClass(), injector.getInstance(entityListProvider.sourceClass()), (Class<Entity>)entityListProvider.entityClass(), provider);
    }

    for(hs.mediasystem.persist.Persister<Entity, ?> persister : injector.getInstances(hs.mediasystem.persist.Persister.class)) {
      EntityPersister entityPersister = persister.getClass().getAnnotation(EntityPersister.class);
      context.registerPersister((Class<Entity>)entityPersister.entityClass(), injector.getInstance(entityPersister.sourceClass()), persister);
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
