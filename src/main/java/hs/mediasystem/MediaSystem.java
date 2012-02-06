package hs.mediasystem;

import javafx.application.Application;

// Top 10 stuff:
// FIXME Fix the reloading of data in Select Media screen
// TODO For Episodes of Series, use background of parent
// TODO Decode images in background thread, not on JavaFX thread
// TODO Titles / Subtitles should not be overriden by enriched data (setting?)

// TODO Use the Message overlay to indicate stuff being loaded in background on Select Media screen
// TODO Actor / Director information in database
// TODO ItemEnrichers really should only return relevant data, not a database Item instance
// TODO Indicate serie somehow when selecting episodes (it is now unclear which serie you are selecting)
// TODO Movie controls (play, pause, volume up/down, etc) should be handled much more globally in ProgramController
// TODO Alice in Wonderland, not a serie, donot group?
// TODO Options Screen: Modal navigation should use own Navigator as well?
// TODO Options Screen: Subtitle selection list must be left with backspace, or exit at top/bottom item
// TODO Make plug-ins of various looks of Select Media
// TODO Remove CellProvider responsibility from MediaTree
// TODO Make plug-ins from MediaTrees
// TODO Hide detail headers when their content is empty
// TODO Perhaps use GridPane on playback overlay instead of hard coded values for the poster size

// TODO Some form of remote control support / Lirc support
// TODO VLCPlayer: Make brightness controls work
// TODO Options Screen: When changing Options, show this in OSD
// TODO Style main page
// TODO Options Screen: Separators for Dialog Screen

// TODO Make subtitles more peristent by storing them with file?
// TODO Add breadcrumb?
// TODO Auto-scroll plot/overview
// TODO Possibly with Series/Season, information about episode count
// TODO BUG: When there's an outstanding query for a provider (which is taking long), it is possible for a 2nd query to be triggered on the same item.  When both return finally, they are inserted one after the other.  The second one causes a duplicate key violation.
// TODO Display alternative banner when there is no banner available

// Unable to Fix:
// - Subtitle list in Video Options should be updated more frequently, especially after downloading a sub (may require Player to notify of changes) --> No indication from VLC when a subtitle actually was fully loaded, so this is not possible

// Keyboard            Action                         On Remote Control
// ====================================================================
// Space             = video: pause/play          --> Pause
// Num4/6            = video: jump +/- 10 secs    --> Ffwd/rew
// Num2/8            = video: jump +/- 60 secs    --> Next/Previous chapter TODO Chapter info might be available, use it?
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
// 'i'               = video: info (display OSD)  --> Info
// 'j'               = video: next subtitle       --> Teletext
// 'o'               = video: show options        --> Options button
// Backspace         = video: exit subwindows     --> Back
// --------------------------------------------------------------------
// Cursor right      = open submenu               --> Right
// Cursor up/down    = navigate up/down           --> Up/down
// Backspace         = back to previous page/menu --> Back
// Enter             = select                     --> OK (nav.center)
// Home              = menu                       --> Home (windows)
// 't'               = video: sub title menu      --> Teletext

// Code conventions:
//
// Method naming:
// - get() methods return null or a checked exception when data is missing; runtime exceptions are thrown for any unexpected errors
// - load() methods never return null and throw a (specific) runtime exception when data is missing or other runtime exceptions for any unexpected errors

public class MediaSystem {

  public static void main(String[] args) {
    Application.launch(FrontEnd.class, args);
  }
}
