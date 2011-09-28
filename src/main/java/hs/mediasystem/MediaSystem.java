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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JComponent;
import javax.swing.Painter;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

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
// TODO Display alternative banner when there is no banner available

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
  
  private static final Painter<Object> DUMMY_PAINTER = new Painter<Object>() {
    @Override
    public void paint(Graphics2D g, Object object, int width, int height) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g.setColor(new Color(128, 0, 0, 32));
      g.fillRect(0, 0, width, height);
    }
  };
  
  public static void main(String[] args) throws UnsupportedLookAndFeelException {
//    UIManager.put("background", new Color(0, 0, 0, 0));
    //UIManager.put("control", new Color(0, 0, 0, 0));
//    UIManager.put("List.opaque", true);
//    UIManager.put("List:\"List.cellRenderer\".opaque", false);
//    UIManager.put("Panel.opaque", true);
//    UIManager.put("Label.opaque", true);
    UIManager.put("ScrollPane.opaque", true);
    UIManager.put("ScrollPane[Enabled].backgroundPainter", new Painter<JComponent>() {
      @Override
      public void paint(Graphics2D g, JComponent object, int width, int height) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0, 0, 0, 32));
        g.fillRect(0, 0, width, height);
      }
    });
    UIManager.put("ScrollPane[Enabled].borderPainter", DUMMY_PAINTER);
    UIManager.put("ScrollPane[Enabled+Focused].borderPainter", DUMMY_PAINTER);
    UIManager.put("List[Enabled].borderPainter", DUMMY_PAINTER);
    UIManager.put("List[Enabled+Focused].borderPainter", DUMMY_PAINTER);
//    UIManager.put("List.border", null);
//    UIManager.put("List.background", new Color(0, 0, 0, 0));
//    UIManager.put("Panel.background", new Color(0, 0, 0, 0));
//    UIManager.put("ScrollPane.background", new Color(0, 255, 0));
//    UIManager.put("TextArea.opaque", true);
    UIManager.put("TextArea.background", new Color(0, 0, 0, 0));
//    UIManager.put("TextArea[Enabled].backgroundPainter",null);
//    UIManager.put("TextArea.disabled", new Color(255, 0, 0, 28));
//    UIManager.put("TextPane.opaque", true);
//    UIManager.put("TextPane.background", new Color(255, 0, 0, 28));
//    UIManager.put("TextPane.disabled", new Color(255, 0, 0, 28));
//    UIManager.put("TextField.background", new Color(0, 0, 0, 0));
    
    UIManager.setLookAndFeel(new NimbusLookAndFeel());
    
//    SynthLookAndFeel laf = new SynthLookAndFeel();
//    UIManager.setLookAndFeel(laf);
    //SynthLookAndFeel.setStyleFactory(new MyStyleFactory());
    

    Controller controller = CONTROLLER_FACTORY.create();
    
    controller.forward(new View("Root", MAIN_MENU));
  }

//  private static class MyStyleFactory extends SynthStyleFactory {
//    @Override
//    public SynthStyle getStyle(JComponent c, Region id) {
//      if(id == Region.BUTTON) {
//        return buttonStyle;
//      }
//      else if(id == Region.TREE) {
//        return treeStyle;
//      }
//      return defaultStyle;
//    }
//  }
  
}
