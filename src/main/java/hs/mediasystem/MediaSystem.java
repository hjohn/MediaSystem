package hs.mediasystem;

import javafx.application.Application;

// Top 10 stuff:
// TODO Decode images in background thread, not on JavaFX thread
// TODO Further refactorings involving StandardLayout
// TODO Focus issues / Keyboard navigation
// TODO [Select Media] Holding down key down in Select Media view causes 100% CPU bug
// TODO [Select Media] TreeView with few items has weird blank area at bottom
// TODO VLCPlayer check if playing with normal items and subitems goes correctly
// TODO Remove MediaType dependency in MediaItem constructor
// TODO Remove MediaType dependency in PlaybackOverlayPane constructor

// TODO ItemEnrichers really should only return relevant data, not a database Item instance
// TODO Alice in Wonderland, not a serie, donot group?
// TODO Options Screen: Modal navigation should use own Navigator as well?
// TODO Options Screen: Subtitle selection list must be left with backspace, or exit at top/bottom item
// TODO Make plug-ins of various looks of Select Media
// TODO Make plug-ins from MediaTrees
// TODO Make plug-ins of subtitle provider
// TODO Perhaps use GridPane on playback overlay instead of hard coded values for the poster size

// TODO Some form of remote control support / Lirc support
// TODO Options Screen: When changing Options, show this in OSD
// TODO Options Screen: Separators for Dialog Screen

// TODO Make subtitles more persistent by storing them with file?

// TODO [Playback] Main overlay only visible when asked for (info)
// TODO [Playback] Mini-volume overlay

// TODO [Select Media] Filtering possibilities (Action movies, Recent movies, etc)
// TODO [Select Media] Remember last selection / scroll position
// TODO [Select Media] Menu to change View layout

// TODO Actor / Director information in database
// TODO Show Actor information somewhere
// TODO Settings screen
// TODO Keep interface between Presentation / View lean -- Should have the means to support alternative layouts without changing Presentation logic

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
