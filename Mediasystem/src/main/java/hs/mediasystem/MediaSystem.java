package hs.mediasystem;

// TODO when loading data of an actor who's image we already got, this happens:
//606.799 ?| AsyncImageProperty - Exception while loading SourceImageHandle(StandardDetailPane://Jim Carrey; fast=true) in background: java.lang.RuntimeException: org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint "images_url"                         -- hs.mediasystem.beans.AsyncImageProperty$2.run(AsyncImageProperty.java:61)
//606.799  |   Detail: Key (url)=(http://cf2.imgobject.com/t/p/original/csFVs03jZKC7RLfiaM3nGjj32L1.jpg) already exists.                         -- hs.mediasystem.beans.AsyncImageProperty$2.run(AsyncImageProperty.java:61)


// TODO Many image loading delay

// TODO Movie Collections should show something fancy on their detail pane, or at the very least display a proper background (Collection view, count, list of items, picture of latest/first (or both), stacked pictures)
// TODO Eliminate MediaRootType
// TODO Order subtitle providers

// TODO Store scroll position

// TODO Banner view -- change layout to get bigger banners -> DEPENDS ON: provider specific detail panes
// TODO Option screen, re-highlight last used option

// Nice to have soon:
// TODO Smart highlight of next-to-see episode --> hard to do because Viewed information is only present after enriching... also, the current system should already remember the last item selected and highlight that again, just press down once..
// TODO Store other movie informations, like subtitle/audio delay, selected subtitle/audio track, that are important when resuming movie
// TODO Make subtitles more persistent by storing them with file (or simply in db?) required if you want to show same sub again after resuming
// TODO Resume functionality; will need to decide if we want ResumePosition in MediaData or put it in SettingsStore (which is loaded at start...)

// Easy stuff:
// TODO Hotkey for download subtitles

// New users:
// TODO Initial settings / Settings screen (configure db)
// TODO Warning if database is not available --> but database is required --> Consider using built-in database
// TODO Detect JavaFX, error dialog if not found

// Other:
// TODO Rename Filter to TabGroup -- or refactor completely to use RadioButton API
// TODO Settings screen
// TODO Subtitle Provider Podnapisi
// TODO [Option Dialog] Modal navigation should use own Navigator as well?
// TODO [Option Dialog] Separators for Dialog Screen
// TODO [Select Media] Filtering possibilities (Action movies, Recent movies, etc)
// TODO [PlaybackOverlay] For Episodes, in playback screen title should be serie name

// Low priority:
// TODO Some form of remote control support / Lirc support --> EventGhost makes this unnecessary, keyboard control suffices
// TODO Investigate why VLC sucks at skipping (audio not working for a while) --> no idea, with hardware decoding it is better but it doesn't skip to key frames then --> this seems media dependent (or processor dependent), occurs with 2012, but not with Game of Thrones

// Considerations:
// - New MediaData with uri and hash is great, but hash collisions can still easily occur when you simply have two copies of the same file

// JavaFX 2.2 issues:
// - Option screen navigation is not showing focus properly, bug in JFX 2.2
// - Empty screen problem.... --> refresh bug it seems, JFX2.2

// VLC issues:
// - Subtitle list in Video Options should be updated more frequently, especially after downloading a sub (may require Player to notify of changes) --> No indication from VLC when a subtitle actually was fully loaded, so this is not possible

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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javafx.application.Application;

public class MediaSystem {
  public static void main(String[] args) {
    Log.initialize(new LinePrinter() {
      private final int methodColumn = 120;
      private final int methodTabStop = 24;
      private final long startTime = System.currentTimeMillis();
      private final String pad = String.format("%" + methodColumn + "s", "");
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
        if(method.startsWith(" -- uk.co.caprica.vlcj.binding.Info.<init>")) {
          return;
        }

        String text = textParm;

        StringBuilder builder = new StringBuilder();
        long sinceStart = System.currentTimeMillis() - startTime;
        long millis = sinceStart % 1000;

        boolean stackTrace = method.startsWith(" -- java.lang.Throwable$WrappedPrintStream.println(Throwable.");
        boolean stackTraceHeader = stackTrace && !text.startsWith("Caused by:") && !text.startsWith("\t");

        if(stackTraceHeader) {
          text = "\r\n" + text;
          stackTrace = false;
        }

        if(!stackTrace) {
          if(sinceStart < 10000) {
            builder.append("    ");
          }
          else if(sinceStart < 100000) {
            builder.append("   ");
          }
          else if(sinceStart < 1000000) {
            builder.append("  ");
          }
          else {
            builder.append(" ");
          }

          builder.append(Long.toString(sinceStart / 1000));
          builder.append(".");
          if(millis < 10) {
            builder.append("00");
          }
          else if(millis < 100) {
            builder.append("0");
          }
          builder.append(Long.toString(millis));
          builder.append(" ");
          builder.append(translateLevel(level));
          builder.append("| ");
        }
        builder.append(text);

        if(builder.length() < methodColumn) {
          builder.append(pad.substring(0, methodColumn - builder.length()));
        }
        else {
          builder.append(pad.substring(0, methodTabStop - builder.length() % methodTabStop));
        }

        if(!stackTrace && !stackTraceHeader) {
          builder.append(method);
        }
        builder.append("\r\n");

        printStream.print(builder.toString());
      }

      private String translateLevel(Level level) {
        return levelMap.get(level);
      }
    });

//    System.setProperty("prism.verbose", "true");
    System.setProperty("prism.lcdtext", "false");
//    System.setProperty("prism.dirtyopts", "false");

    Application.launch(FrontEnd.class, args);
  }
}
