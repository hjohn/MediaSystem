package hs.mediasystem;

import hs.mediasystem.framework.AbstractBlock;
import hs.mediasystem.framework.Extensions;
import hs.mediasystem.framework.Screen;
import hs.mediasystem.framework.View;
import hs.mediasystem.players.mplayer.MPlayerControllerFactory;
import hs.mediasystem.screens.Clock;
import hs.mediasystem.screens.Header;
import hs.mediasystem.screens.MainMenu;
import hs.mediasystem.screens.MediaSystemBorder;
import hs.mediasystem.screens.movie.MediaSelection;
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
// TODO Download + show subtitles, also multiple different ones should work without problem; store them with file I guess
// TODO Lirc support
// TODO Something to show length of movie/time left
// TODO Brightness adjustment
// TODO Include year in duoline
// TODO Groups (Seasons, Collections) should be openable once -> solve by going to tree?
// TODO Add breadcrumb
// TODO Auto-scroll plot/overview
// TODO Possibly with Series/Season, information about episode count
// TODO Display Genre, Rating, First Aired, etc.. for all types
// TODO BUG: When there's an outstanding query for a provider (which is taking long), it is possible for a 2nd query to be triggered on the same item.  When both return finally, they are inserted one after the other.  The second one causes a duplicate key violation.
// TODO Hate the ugly Renderer class where still use Swing components setPreferredSize to make proper sized elements for a JList

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
  public static final Screen MAIN_MENU;
  public static final Screen MEDIA_SELECTION;
  public static final Screen VIDEO_PLAYING;
  public static final Screen VIDEO_OPTIONS;
  
  private static final ControllerFactory CONTROLLER_FACTORY;
  
  static {
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
    
    CONTROLLER_FACTORY = new MPlayerControllerFactory(Paths.get(section.get("mplayer.path")));
        
    final AbstractBlock<?> mediaSystemBorder = new MediaSystemBorder();
    final AbstractBlock<?> header = new Header();
    final AbstractBlock<?> clock = new Clock();
    final MainMenu mainOptions = new MainMenu(moviesPath, seriesPath);
    final MediaSelection mediaSelection = new MediaSelection();
    
    MAIN_MENU = new Screen(mediaSystemBorder, new Extensions() {{
      addExtension("top", new Screen(header));
      addExtension("center", new Screen(mainOptions));
      addExtension("bottom", new Screen(clock));
    }});
    
    MEDIA_SELECTION = new Screen(mediaSystemBorder, new Extensions() {{
      addExtension("top", new Screen(header));
      addExtension("center", new Screen(mediaSelection));
      addExtension("bottom", new Screen(clock));
    }});
        
//    controller.registerScreen("MainMenu", new MainMenu(controller));
    //controller.registerScreen("MovieMenu", new MovieMenu(controller));
    VIDEO_PLAYING = new Screen(new VideoPlayingMenu(new SublightSubtitleClient(sublightClientName, sublightKey)));

    VIDEO_OPTIONS = new Screen(new VideoOptionsScreen());
  }
  
  public static void main(String[] args) {
    Controller controller = CONTROLLER_FACTORY.create();
    
    controller.forward(new View("Root", MAIN_MENU));
  }
}
