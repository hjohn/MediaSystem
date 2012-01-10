package hs.mediasystem;

import hs.mediasystem.db.Cachable;
import hs.mediasystem.db.CachedItemEnricher;
import hs.mediasystem.db.CachedItemIdentifier;
import hs.mediasystem.db.ItemEnricher;
import hs.mediasystem.db.ItemIdentifier;
import hs.mediasystem.db.TmdbMovieEnricher;
import hs.mediasystem.db.TvdbEpisodeEnricher;
import hs.mediasystem.db.TvdbSerieEnricher;
import hs.mediasystem.db.TvdbSerieIdentifier;
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

import javafx.application.Application;
import javafx.stage.Stage;
import net.sf.jtmdb.GeneralSettings;

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
    Module module = new AbstractModule() {
      @Override
      protected void configure() {
        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(MoviesMainMenuExtension.class);
        Multibinder.newSetBinder(binder(), MainMenuExtension.class).addBinding().to(SeriesMainMenuExtension.class);

        MapBinder.newMapBinder(binder(), String.class, ItemEnricher.class).addBinding("SERIE").to(TvdbSerieEnricher.class);
        MapBinder.newMapBinder(binder(), String.class, ItemEnricher.class).addBinding("MOVIE").to(TmdbMovieEnricher.class);
        MapBinder.newMapBinder(binder(), String.class, ItemEnricher.class).addBinding("EPISODE").to(TvdbEpisodeEnricher.class);

        bind(ItemIdentifier.class).to(CachedItemIdentifier.class);
        bind(ItemIdentifier.class).annotatedWith(Cachable.class).to(TvdbSerieIdentifier.class);

        bind(ItemEnricher.class).to(CachedItemEnricher.class);
        bind(ItemEnricher.class).annotatedWith(Cachable.class).to(TypeBasedItemEnricher.class);
      }

      @Provides
      public Player providesPlayer() {
        return player;
      }

      @Provides
      public Ini providesIni() {
        return INI;
      }
    };

    Injector injector = Guice.createInjector(module);

    ProgramController controller = injector.getInstance(ProgramController.class);

    controller.showMainScreen();
  }
}
