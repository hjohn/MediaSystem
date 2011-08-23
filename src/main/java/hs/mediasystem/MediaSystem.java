package hs.mediasystem;

import hs.mediasystem.players.mplayer.MPlayerControllerFactory;
import hs.mediasystem.screens.AbstractBlock;
import hs.mediasystem.screens.Clock;
import hs.mediasystem.screens.Header;
import hs.mediasystem.screens.MainMenu;
import hs.mediasystem.screens.MediaSystemBorder;
import hs.mediasystem.screens.movie.MovieMenu;
import hs.mediasystem.screens.videoplaying.VideoOptionsScreen;
import hs.mediasystem.screens.videoplaying.VideoPlayingMenu;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;
import hs.sublight.SublightSubtitleClient;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import net.sf.jtmdb.GeneralSettings;

// Minimal working system:
// TODO Make clock in standard border
// TODO ListBox as basis instead of JTable
// TODO Series display
// TODO Download + show subtitles
// TODO Lirc support
// TODO Something to show length of movie/time left
// TODO Subtitle timing adjustment
// TODO Brightness adjustment
// TODO Mute/Volume controls

// TODO Display list of video files with IMDB info

// Key                 Action                         On Remote Control
// ====================================================================
// ' '               = video: pause/play          --> Pause
// Cursor up/down    = video: ffwd/rew            --> Ffwd/rew
// Cursor left/right = video: jump +/- 10 secs    --> Left/right
// 's'               = video: stop                --> Stop
// --------------------------------------------------------------------
// Cursor right      = open submenu               --> Right
// Cursor up/down    = navigate up/down           --> Up/down
// Backspace         = back to previous page/menu --> Back
// Enter             = select                     --> OK (nav.center)
// Home              = menu                       --> Home (windows)

public class MediaSystem {
  
  public static void main(String[] args) {
    Ini ini = new Ini(new File("mediasystem.ini"));
    
    Section section = ini.getSection("general");
    
    Path libraryPath = Paths.get(section.get("movies.path"));
    //section.get("temp.path");
    String sublightKey = section.get("sublight.key");
    String sublightClientName = section.get("sublight.client");
    
    GeneralSettings.setApiKey(section.get("jtmdb.key"));
    GeneralSettings.setLogEnabled(true);
    GeneralSettings.setLogStream(System.out);
    
    //new VLCMainFrame();
    ControllerFactory controllerFactory = new MPlayerControllerFactory(Paths.get(section.get("mplayer.path")));
    
    Controller controller = controllerFactory.create();

    final AbstractBlock mediaSystemBorder = new MediaSystemBorder();
    final AbstractBlock header = new Header();
    final AbstractBlock clock = new Clock();
    final MainMenu mainOptions = new MainMenu();
    final MovieMenu movieSelection = new MovieMenu(libraryPath);
    
//    controller.registerScreen("MainMenu", new Configuration(mediaSystemBorder) {{
//      setExtension(MediaSystemBorder.Extension.TOP, new Configuration(header));
//      setExtension(MediaSystemBorder.Extension.CENTER, new Configuration(mainOptions));
//      setExtension(MediaSystemBorder.Extension.BOTTOM, new Configuration(clock));
//    }});
    
    controller.registerScreen("MainMenu", new Screen(mediaSystemBorder, new Extensions() {{
      addExtension("top", new Screen(header));
      addExtension("center", new Screen(mainOptions));
      addExtension("bottom", new Screen(clock));
    }}));
    
    controller.registerScreen("MovieMenu", new Screen(mediaSystemBorder, new Extensions() {{
      addExtension("top", new Screen(header));
      addExtension("center", new Screen(movieSelection));
      addExtension("bottom", new Screen(clock));
    }}));
    
//    controller.registerScreen("MainMenu", new MainMenu(controller));
    //controller.registerScreen("MovieMenu", new MovieMenu(controller));
    controller.registerScreen("VideoPlayingMenu", new Screen(new VideoPlayingMenu(new SublightSubtitleClient(sublightClientName, sublightKey))));

    controller.registerScreen("VideoOptions", new Screen(new VideoOptionsScreen()));
    
    controller.forward("MainMenu");
  }
}
