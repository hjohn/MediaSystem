package hs.mediasystem;

import javafx.application.Application;

import com.sun.jna.NativeLibrary;

// Top 10 stuff:
// TODO Subtitle selection / Auto download
// TODO Some form of remote control support
// TODO VLCPlayer: Make brightness controls work
// TODO When changing Options, show this in OSD
// TODO Tree selection bug (expand node, double click first newly shown item, plays wrong item).  Looks like BUG RT-16565.
// TODO Background picture is centered, not stretched
// TODO When returning to select screen, if bg pic is big, the scene will be larger than the stage
// TODO Style main page

// Minimal working system:
// TODO Make clock in standard border
// TODO Download + show subtitles, also multiple different ones should work without problem; store them with file I guess
// TODO Lirc support
// TODO Include year in duoline
// TODO Add breadcrumb
// TODO Auto-scroll plot/overview
// TODO Possibly with Series/Season, information about episode count
// TODO Display Genre, Rating, First Aired, etc.. for all types
// TODO BUG: When there's an outstanding query for a provider (which is taking long), it is possible for a 2nd query to be triggered on the same item.  When both return finally, they are inserted one after the other.  The second one causes a duplicate key violation.
// TODO Display alternative banner when there is no banner available

// TODO Display list of video files with IMDB info

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
    NativeLibrary.addSearchPath("libvlc", "c:/program files (x86)/VideoLAN/VLC");
    NativeLibrary.addSearchPath("libvlc", "c:/program files/VideoLAN/VLC");

    Application.launch(FrontEnd.class, args);
  }
}
