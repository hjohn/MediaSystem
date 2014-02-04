package hs.mediasystem;

// TASK Rename Providers to indicate they are usng the database
// TASK Empty big detail pane, leads to NPE when pressing back
// TASK FinishEnrichCallback seems the same everywhere... if not needed can also make better logging
// TASK Persisters might be able to become part of Provider...
// TASK If we change Setting to work with Entity.. then DefaultPersistable can be killed + Setting dao can be returned to normal

// TASK Serie without banner doesn't display anything anymoreiu
// TASK Movie recommendations
// TASK Get Biography/Birthdate information always from TMDB

// TASK Possible to navigate to empty cell at bottom right in two column banner pane
// TASK Series view, tabs, recently watched...

// TASK Think about title/subtitle/collections not being part of Item...

// TASK [Trivial] [Considering] CLEANUP-AFTER-REFACTOR: Add version check
// TASK [Trivial] Make better use of BundleContext in ProgramController (by classes that have a reference to ProgramController)
// TASK [Easy] Make default screen number configureable using a Setting --> perhaps just save position automatically (after CTRL-ALT-S press)

// TASK [DetailPane] [Easy] Add Play button in Big Info screen
// TASK [DetailPane] CLEANUP-AFTER-REFACTOR: Add bypasscache -> need reload meta data option back first

// TASK [Trivial] [Considering] Perhaps rename MediaItem to MediaStream class, to make it more clear it represents local/remote streams -- Media class is for Meta Data only, no local data at all
// TASK CLEANUP-AFTER-REFACTOR: Tasks should display on Task Overlay -> yes...
// TASK How to order Person->Movie castings? -> Hard to keep it sorted with Comparator as Items might still not be loaded...

// TASK [DetailPane] Make Castings Row a scrollable control
// TASK [DetailPane] Files with a db-entry should have Media.class, and those without shouldn't; DetailPanes should be created for these two situations seperately, as well as Cells
// TASK [DetailPane] [Trivial] [Considering] Make (actor photo) focus more clearly visible (animation perhaps)
// TASK [DetailPane] Movie Collections should show something fancy on their detail pane, or at the very least display a proper background (Collection view, count, list of items, picture of latest/first (or both), stacked pictures)

// TASK [Trivial] [Considering] Changes for fullscreen display that were lost when switching branches
// TASK Locations should support specific highlighting of an item (it may be possible to also put the last-selected code in there)
// TASK Sort out packages
// TASK Controller.play() might be replaceable with Location

// TASK Move stylesheets to bundles
// TASK Possibly allow focus in multi-line text and actors boxes, for page navigation (dots below the control show number of pages and active page)
// TASK Introduce back + navigation + select events ; look at navigation in DialogPane... use navigator or use custom backspace handler

// TASK Create downloadable jar

// TASK [Bug] Cells in TreeView sometimes become super wide after switching screens (to different resolutions)
// TASK [Easy] [Considering] Change pause/mute overlays
// TASK [Easy] PathSelectOption: Start at last selected path
// TASK [Easy] BannerListPane and other ListPane really should externalize the cell provider to remove bundlecontext

// TASK [Considering] Store scroll position (requires hacking skin)

// TASK Banner view -- change layout to get bigger banners -> DEPENDS ON: provider specific detail panes -> perhaps provide detail panes of varying sizes (1/2, 1/3, 1/4)
// TASK [Easy] Option screen: re-highlight last used option

// Big stuff:
// TASK Movie gallery layout ext (cover flow)
// TASK Resume functionality
//      - Look into SettingsStore PersistLevel system; determine useability for storing settings per video (like resume)
//      - Make subtitles more persistent by storing them with file (or simply in db?) required if you want to show same sub again after resuming
//      - Add extra fields to MediaData, like subtitle/audio delay, selected subtitle/audio track, that are important when resuming movie
//      - Actually setting all player parameters to correct positions
// TASK Improved Special Handling, including real specials and just bonus stuff

// OSGI:
// TASK [Bug] Reloading of Bundles fails, needs a fix
// TASK Externalize more of the services being registered in FrontEnd

// Other:
// TASK [Easy] [Considering] Rename Filter to TabGroup -- or refactor completely to use RadioButton API
// TASK Subtitle Provider Podnapisi
// TASK [SelectMedia] Filtering possibilities (Action movies, Recent movies, etc)

// Low priority:
// TASK Some form of remote control support / Lirc support --> EventGhost makes this unnecessary, keyboard control suffices

// Possible Configuration Options:
// - Default Screen number
// - Default player
// - Default visibility of info bar on playback screen

// Considerations:
// - New MediaData with uri and hash is great, but hash collisions can still easily occur when you simply have two copies of the same file

// JavaFX 2.2 issues:
// - Empty screen problem.... --> refresh bug it seems, JFX2.2
// - StackPane with Messages does not appear --> bug in JFX2.2
// - Enrich is triggered for every item upto the current position... WTF! --> JFX problem, TreeView accessing all cells during scrollTo()

// VLC issues:
// - Subtitle list in Video Options should be updated more frequently, especially after downloading a sub (may require Player to notify of changes) --> No indication from VLC when a subtitle actually was fully loaded, so this is not possible
// - Investigate why VLC sucks at skipping (audio not working for a while) --> no idea, with hardware decoding it is better but it doesn't skip to key frames then --> this seems media dependent (or processor dependent), occurs with 2012, but not with Game of Thrones --> VLC bug

// General Annoyances:
// - Creating a new Stage and displaying it when app does not have focus causing Windows to flash the taskbar button (does this too for switching between transparent and non-transparent stage... perhaps transparent performance is acceptable now...?)
// - Derby: Problem with savepoints getting rolled back DERBY-5545/DERBY-5921 --> "solved" now by commiting read only transactions by default

// -verbose:gc -XX:+PrintGCTimeStamps -XX:+PrintGCDetails

// Keyboard            Action                         On Remote Control
// ====================================================================
// Space             = video: pause/play          --> Pause
// Num4/6 Cursor l/r = video: jump +/- 10 secs    --> Left/Right
// Num2/8 Cursor u/d = video: jump +/- 60 secs    --> Up/Down
// 's'               = video: stop                --> Stop
//
// '9'               = video: volume down         --> Volume Down
// '0'               = video: volume up           --> Volume Up
// 'm'               = video: mute                --> Mute
//
// '1'               = video: brightness down     --> Previous Channel
// '2'               = video: brightness up       --> Next Channel
// '['               = video: speed -10%          -->
// ']                = video: speed +10%          -->
// 'i'               = video: info (display OSD)  --> Info
//
// 'j'               = video: next subtitle       --> Teletext
// 't'               = video: sub title menu      --> Teletext (long press)
// 'x'               = video: subtitle -0.1       --> Clear
// 'z'               = video: subtitle +0.1       --> Enter
//
// 'o'               = video: show options        --> Options button
// Backspace         = video: exit subwindows     --> Back
// --------------------------------------------------------------------
// Cursor left/right = change filter              --> Left/Right
// Cursor up/down    = navigate up/down           --> Up/down
// Page up/down      = navigate page up/down      --> Channel up/down
// Backspace         = back to previous page/menu --> Back
// Enter             = select                     --> OK (nav.center)
// Home              = menu                       --> Home (windows)
// 'o'               = screen options
// 'c'               = context options
// CTRL+ALT+S        = switch screen

// Code conventions:
//
// Method naming:
// - get() methods return null or a checked exception when data is missing; runtime exceptions are thrown for any unexpected errors
// - load() methods never return null and throw a (specific) runtime exception when data is missing or other runtime exceptions for any unexpected errors

// Log levels:
// - SEVERE  :
// - WARNING :
// - INFO    : For any user interaction, key presses, mouse clicks, etc. but not scrolling (mouse drags, scroll wheel)
// Levels INFO and above should never occur when application is not actively being used, unless these events were triggered earlier (background process) or scheduled by the user
// - CONFIG  : Configuraton information, like user set parameters -- these probably should never be printed unless there's also a higher level being printed nearby
// - FINE    : Debug code

// Video Players that can be integrated with Java
// ==============================================
// * DSJ (DirectShow)
//   - Performance: Good
//   - Integration: Canvas
//   - Subtitles: No clue how to select, no clue how to provide my own
//   - Audiostreams: see subtitles
//   - Communication: Java, low-level interface
//   - Problems: Almost impossible to control due to DirectShow architecture, which means no control of internal subtitles or audio streams
// * GStreamer-java (GStreamer)
//   - Performance: Suffices on Quad Core Xeon
//   - Integration: Canvas
//   - Subtitles: ?
//   - Audiostreams: ?
//   - Communication: Java, low-level interface
//   - Stability: Crashes after 5-10 minutes
// * MPlayer
//   - Performance: Normally excellent, but for some reason a bit flakey on my system atm
//   - Integration: Create your own window and obtain its window id and provide this to MPlayer
//   - Subtitles: both internal and external supported
//   - Audiostreams: works with multiple audio streams and can switch between them
//   - Communication: STD in/out, high-level interface
//   - Problems: The communication only accepts one command per frame displayed, which can severely limit the speed at which MPlayer reacts to commands -- care must be taken to only sent the bare minimum of commands and glean the rest of the information as much as possible from its status messages and status line
// * VLCJ (VLC)
//   - Performance: Good in general, seek performance excellent with new CPU, no audio for a few seconds after seeking (seems media dependent)
//   - Integration: Canvas
//   - Subtitles: both internal and external supported
//   - Audiostreams: works with multiple audio streams and can switch between them
//   - Communication: Java, high-level interface
// * JavaFX
//   - Performance: ?
//   - Integration: Good
//   - Subtitles: ?
//   - Audiostreams: ?
//   - Communication: Java, high-level interface
//   - Problems: Only supports incredibly limited set of formats.  No MKV support means it is a non-starter.
// * MediaPlayer Classic Home Cinema
//   - Performance: Good
//   - Integration: Uses its own window which must be in fullscreen mode -- this gives problems as my JavaFX overlay cannot do fullscreen mode properly yet on secondary displays
//   - Subtitles: Untested, looks like unable to provide external subtitle
//   - Audiostreams: Untested, but looking good
//   - Communication: HWND, high-level interface
//   - Problems: Uses its own window (can use /monitor switch and /fullscreen switch to control it a bit); cannot disable user control
// * Xuggler (FFMPEG)
//   - Performance: May be good with properly threaded audio/video pipelines
//   - Integration: Works with Swing, should be intergratable
//   - Subtitles: DIY
//   - Audiostreams: Should support multiple audio streams, the problem is with playback, only Stereo is supported (Xuggler or Java Audio limitation)
//   - Communication: Java, low-level interface
//   - Problems: A lot of stuff needs to be done yourself, including creating a frame decoding loop with multiple threads (have working test case); subtitles will need to be decoded yourself; audio looks to be limited to only stereo

// Plugins
// =======
//
// MoviesMainMenuExtension
//   - enrichers
//   - StandardView
//
// HorizontalCellTypeExtension
//   provides: HorizontalCellType
//
// MovieCellProviderExtension
//   requires: MoviesMainMenuExtension
//   requires: HorizontalCellTypeExtension
//   requires: DuoLineCellExtension
//   provides: MovieCellProvider
//
// TreeListPaneExtension
//   requires: HorizontalCellTypeExtension
//
// File recognition
// ================
// * store full path, filesize, file creation date, hash
//
// Normal Flow:
// - Matches on Path, Filesize, Creation Date (QUICK MATCH)
//
// Filesize and or Creation Date changed (transcoded file):
// - Matches on Path (QUICK MATCH)
// - Update Filesize, Creation Date, Hash
//
// Moved File with good partial path match (at very least file name must match, perhaps see if it has enough entropy) [optional advanced matcher]:
// - Matches on Filesize, Creation Date and partial Path match (QUICK MATCH)
// - Update Path
//
// Moved File unable to match without hashing (Hashing is relatively expensive so should be used sparingly):
// - No match found (or only matches that match on Filesize)
// - Calculate Hash as we are probably going to want to create a new entry anyway
// - Check Hash against DB
// - Found Hash match (LOCAL MATCH)
// - Update Path, Creation Date (Filesize not needed as this must match already as it is included in Hash)
//
// New File:
// - No match found
// - Calculate Hash as we are probably going to want to create a new entry anyway
// - Check Hash against DB
// - No Hash match, must be new file (NEW ENTRY)

import hs.mediasystem.util.Log;
import hs.mediasystem.util.Log.LinePrinter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;

import javafx.application.Application;

public class MediaSystem {
  public static void main(String[] args) throws SecurityException, FileNotFoundException, IOException {
    ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(true);

    Log.initialize(new LinePrinter() {
      private static final int METHOD_COLUMN = 120;
      private static final int METHOD_TAB_STOP = 24;

      private final long startTime = System.currentTimeMillis();
      private final String pad = String.format("%" + METHOD_COLUMN + "s", "");
      private final Map<Level, String> levelMap = new HashMap<Level, String>() {{
        put(Level.SEVERE, "!");
        put(Level.WARNING, "?");
        put(Level.INFO, "*");
        put(Level.CONFIG, "+");
        put(Level.FINE, " ");
        put(Level.FINER, "-");
        put(Level.FINEST, "=");
      }};

      @Override
      public void print(PrintStream printStream, Level level, String textParm, String method) {
        if(method.startsWith(" -- uk.co.caprica.vlcj.Info.<init>")) {
          return;
        }

        String text = textParm;

        StringBuilder builder = new StringBuilder();
        long sinceStart = System.currentTimeMillis() - startTime;

        boolean stackTrace = method.startsWith(" -- java.lang.Throwable$WrappedPrintStream.println(");
        boolean stackTraceHeader = stackTrace && !text.startsWith("Caused by:") && !text.startsWith("\t");

        if(stackTraceHeader) {
          text = "\r\n" + text;
          stackTrace = false;
        }

        if(!stackTrace) {
          Runtime runtime = Runtime.getRuntime();
          long usedMemory = runtime.totalMemory() - runtime.freeMemory();

          builder.append(String.format("%1s%9.3f\u2502%3d\u2502%02x\u2502 ", translateLevel(level), ((double)sinceStart) / 1000, usedMemory / 1024 / 1024, Thread.currentThread().getId()));
        }
        builder.append(text);

        if(!stackTrace && !stackTraceHeader) {
          if(builder.length() < METHOD_COLUMN) {
            builder.append(pad.substring(0, METHOD_COLUMN - builder.length()));
          }
          else {
            builder.append(pad.substring(0, METHOD_TAB_STOP - builder.length() % METHOD_TAB_STOP));
          }
          builder.append(method);
        }
        builder.append("\r\n");

        printStream.print(builder.toString());
      }

      private String translateLevel(Level level) {
        return levelMap.get(level);
      }
    });

    try(FileInputStream stream = new FileInputStream("logging.properties")) {
      LogManager.getLogManager().readConfiguration(stream);
    }
    catch(FileNotFoundException e) {
      System.out.println("[INFO] File 'logging.properties' not found, using defaults");
    }


    System.setProperty("prism.lcdtext", "false");
//    System.setProperty("prism.verbose", "true");
//    System.setProperty("prism.dirtyopts", "false");

    Application.launch(FrontEnd.class, args);
  }
}