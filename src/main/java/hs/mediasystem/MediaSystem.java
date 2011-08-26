package hs.mediasystem;

import hs.mediasystem.players.mplayer.MPlayerControllerFactory;
import hs.mediasystem.screens.AbstractBlock;
import hs.mediasystem.screens.Clock;
import hs.mediasystem.screens.Header;
import hs.mediasystem.screens.MainMenu;
import hs.mediasystem.screens.MediaSystemBorder;
import hs.mediasystem.screens.movie.MovieMenu;
import hs.mediasystem.screens.movie.MovieMenu.Mode;
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
// TODO Series display
// TODO Download + show subtitles, also multiple different ones should work without problem; store them with file I guess
// TODO Lirc support
// TODO Something to show length of movie/time left
// TODO Brightness adjustment

// TODO Display list of video files with IMDB info

// Keyboard            Action                         On Remote Control
// ====================================================================
// Space             = video: pause/play          --> Pause
// Num4/6            = video: jump +/- 10 secs    --> Ffwd/rew
// Num2/8            = video: jump +/- 60 secs    --> Next/Previous chapter
// 's'               = video: stop                --> Stop
// '9'               = video: volume down         --> Volume Down
// '0'               = video: volume up           --> Volume Up
// '1'               = video: brightness down     --> Previous Channel
// '2'               = video: brightness up       --> Next Channel
// 'x'               = video: subtitle -0.1       --> Left
// 'z'               = video: subtitle +0.1       --> Right
// '['               = video: speed -10%          --> Down
// ']                = video: speed +10%          --> Up
// 'm'               = video: mute                --> Mute
// 'i'               = video: info (display time) --> Info
// --------------------------------------------------------------------
// Cursor right      = open submenu               --> Right
// Cursor up/down    = navigate up/down           --> Up/down
// Backspace         = back to previous page/menu --> Back
// Enter             = select                     --> OK (nav.center)
// Home              = menu                       --> Home (windows)
// 't'               = video: sub title menu      --> Teletext 

public class MediaSystem {
  
  public static void main(String[] args) {
    Ini ini = new Ini(new File("mediasystem.ini"));
    
    Section section = ini.getSection("general");
    
    Path moviesPath = Paths.get(section.get("movies.path"));
    Path seriesPath = Paths.get(section.get("series.path"));
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
    final MovieMenu movieSelection = new MovieMenu(moviesPath, Mode.LIST);
    final MovieMenu serieSelection = new MovieMenu(seriesPath, Mode.LIST);
    
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
    
    controller.registerScreen("SerieSelection", new Screen(mediaSystemBorder, new Extensions() {{
      addExtension("top", new Screen(header));
      addExtension("center", new Screen(serieSelection));
      addExtension("bottom", new Screen(clock));
    }}));

    
//    controller.registerScreen("MainMenu", new MainMenu(controller));
    //controller.registerScreen("MovieMenu", new MovieMenu(controller));
    controller.registerScreen("VideoPlayingMenu", new Screen(new VideoPlayingMenu(new SublightSubtitleClient(sublightClientName, sublightKey))));

    controller.registerScreen("VideoOptions", new Screen(new VideoOptionsScreen()));
    
    controller.forward("MainMenu");
  }
}
