package hs.mediasystem;

import javafx.application.Application;

//[FINE] CachedItemEnricher.enrichItem() - Enrichment failed: com.google.inject.ProvisionException: Guice provision errors:
//
//1) Error in custom provider, java.lang.IllegalStateException: Interrupted while waiting for a database connection
//  at hs.mediasystem.FrontEnd$1.providesConnection(FrontEnd.java:113)
//  while locating java.sql.Connection
//
//1 error

// Top 10 stuff:
// TODO Provide sequence numbers for sub-items to distinguish movies
// TODO Change the Option overlay to be generic
// TODO Use the Message overlay to indicate stuff being loaded in background on Select Media screen
// TODO Split off pictures from meta-data information for faster loading
// TODO Actor / Director information in database
// TODO ItemEnrichers really should only return relevant data, not a database Item instance
// TODO Add memory indicator / time / date / Make clock in standard border
// TODO Make NavigationItem/History more generic; inject it into Presentations and allow it to be used as global navigation
// TODO Indicate serie somehow when selecting episodes (it is now unclear which serie you are selecting)

// TODO Some form of remote control support / Lirc support
// TODO VLCPlayer: Make brightness controls work
// TODO When changing Options, show this in OSD
// TODO Style main page
// TODO Separators for Dialog Screen
// TODO Subtitle selection list must be left with backspace, or exit at top/bottom item

// TODO Solve hack in SelectMediaPresentation to only load one item at the time in the background
// TODO Local names not shown everywhere(?)

// Minimal working system:
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

public class MediaSystem {

  public static void main(String[] args) {
    Application.launch(FrontEnd.class, args);
  }
}
