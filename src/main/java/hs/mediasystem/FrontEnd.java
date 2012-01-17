package hs.mediasystem;

import hs.mediasystem.db.Cachable;
import hs.mediasystem.db.CachedItemEnricher;
import hs.mediasystem.db.ConnectionPool;
import hs.mediasystem.db.ItemEnricher;
import hs.mediasystem.db.TmdbMovieEnricher;
import hs.mediasystem.db.TvdbEpisodeEnricher;
import hs.mediasystem.db.TvdbSerieEnricher;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MoviesMainMenuExtension;
import hs.mediasystem.screens.SeriesMainMenuExtension;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.sql.Connection;
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
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

public class FrontEnd extends Application {
  private static final Ini INI = new Ini(new File("mediasystem.ini"));

  private Player player;
  private ConnectionPool pool;

  @Override
  public void init() throws Exception {
    Section section = INI.getSection("general");

//    Path moviesPath = Paths.get(section.get("movies.path"));
//    Path seriesPath = Paths.get(section.get("series.path"));
//    String sublightKey = section.get("sublight.key");
//    String sublightClientName = section.get("sublight.client");

    GeneralSettings.setApiKey(section.get("jtmdb.key"));
    GeneralSettings.setLogEnabled(false);
    GeneralSettings.setLogStream(System.out);

    String factoryClassName = section.getDefault("player.factoryClass", "hs.mediasystem.players.vlc.VLCPlayerFactory");

    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] gs = ge.getScreenDevices();

    int screen = Integer.parseInt(INI.getSection("general").getDefault("screen", "0"));
    GraphicsDevice graphicsDevice = (screen >= 0 && screen < gs.length) ? gs[screen] : gs[0];

    System.out.println("Using display: " + graphicsDevice + "; " + graphicsDevice.getDisplayMode().getWidth() + "x" + graphicsDevice.getDisplayMode().getHeight() + "x" + graphicsDevice.getDisplayMode().getBitDepth() + " @ " + graphicsDevice.getDisplayMode().getRefreshRate() + " Hz");

    PlayerFactory playerFactory = (PlayerFactory)Class.forName(factoryClassName).newInstance();
    player = playerFactory.create(INI, graphicsDevice);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    PGConnectionPoolDataSource dataSource = new PGConnectionPoolDataSource();

    Section db = INI.getSection("database");

    dataSource.setServerName(db.getDefault("host", "127.0.0.1"));
    dataSource.setPortNumber(Integer.parseInt(db.getDefault("port", "5432")));
    dataSource.setDatabaseName(db.getDefault("name", "mediasystem"));
    dataSource.setPassword(db.get("password"));
    dataSource.setUser(db.get("user"));

    pool = new ConnectionPool(dataSource, 5);

    Module module = new AbstractModule() {
      @Override
      protected void configure() {
        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(MoviesMainMenuExtension.class);
        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(SeriesMainMenuExtension.class);

        MapBinder.newMapBinder(binder(), String.class, ItemEnricher.class).addBinding("SERIE").to(TvdbSerieEnricher.class);
        MapBinder.newMapBinder(binder(), String.class, ItemEnricher.class).addBinding("MOVIE").to(TmdbMovieEnricher.class);
        MapBinder.newMapBinder(binder(), String.class, ItemEnricher.class).addBinding("EPISODE").to(TvdbEpisodeEnricher.class);

        bind(ItemEnricher.class).to(CachedItemEnricher.class);
        bind(ItemEnricher.class).annotatedWith(Cachable.class).to(TypeBasedItemEnricher.class);
      }

      @Provides @SuppressWarnings("unused")
      public Player providesPlayer() {
        return player;
      }

      @Provides @SuppressWarnings("unused")
      public Ini providesIni() {
        return INI;
      }

      @Provides @SuppressWarnings("unused")
      public Connection providesConnection() {
        Connection connection = pool.getConnection();

        try {
          connection.prepareStatement("SET search_path = public").execute();
        }
        catch(SQLException e) {
          throw new RuntimeException(e);
        }

        return connection;
      }
    };

    Injector injector = Guice.createInjector(module);

    ProgramController controller = injector.getInstance(ProgramController.class);

    controller.showMainScreen();
  }

  @Override
  public void stop() throws Exception {
    if(pool != null) {
      pool.close();
    }
  }
}
