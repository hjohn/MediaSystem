package hs.mediasystem.screens.videoplaying;

import hs.mediasystem.Controller;
import hs.mediasystem.MediaSystem;
import hs.mediasystem.framework.AbstractBlock;
import hs.mediasystem.framework.NoConfig;
import hs.mediasystem.framework.View;
import hs.mediasystem.fs.Episode;
import hs.smartlayout.Anchor;
import hs.sublight.SublightSubtitleClient;
import hs.sublight.SubtitleDescriptor;
import hs.ui.AcceleratorScope;
import hs.ui.ControlListener;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.Button;
import hs.ui.controls.HorizontalGroup;
import hs.ui.controls.Label;
import hs.ui.controls.VerticalGroup;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.List;

import javax.swing.KeyStroke;

public class VideoPlayingMenu extends AbstractBlock<NoConfig> {
  private final SublightSubtitleClient client;

  public VideoPlayingMenu(SublightSubtitleClient client) {
    this.client = client;
  }
  
  @Override
  protected AbstractGroup<?> create(final Controller controller) {
    return new VerticalGroup() {{      
      setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), AcceleratorScope.ANCESTOR_WINDOW, new ControlListener<VerticalGroup>() {
        @Override
        public void onEvent(VerticalGroup control) {
          if(controller.getCurrentItem() instanceof Episode) { // TODO only useful to get subtitles for non-streamed media
            System.out.println("Home was pressed");
            
            Integer year = null;
            try {
              year = Integer.parseInt(((Episode)controller.getCurrentItem()).getYear());
            }
            catch(NumberFormatException e) {
            }

            Short season = (short)controller.getCurrentItem().getSeason(); 
            Integer episode = controller.getCurrentItem().getEpisode();
            
            if(season == 0) {
              season = null;
              episode = null;
            }

            System.out.println("Looking for subtitles: " + controller.getCurrentItem().getTitle() + "; " +  year + "; " + season + "; " + episode + "; English");

            List<SubtitleDescriptor> subtitleList2 = 
              client.getSubtitleList(controller.getCurrentItem().getTitle(), year, season, episode, "English");
  //          client.getSubtitleList(controller.getCurrentItem().getTitle(), null, (short)1, 3, "English");
            System.out.println(subtitleList2);
            
            controller.subtitles.clear();
            controller.subtitles.addAll(subtitleList2);
            
            controller.forward(new View("Playback Options", MediaSystem.VIDEO_OPTIONS));
          }
        }
      });
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
//      setAccelerator(KeyStroke.getKeyStroke('9'), AcceleratorScope.ANCESTOR_WINDOW, new ControlListener<VerticalGroup>() {
//        @Override
//        public void onEvent(VerticalGroup control) {
//          throw new UnsupportedOperationException("Method not implemented");
//        }
//      });
      
      weightX().set(1.0);
      weightY().set(1.0);
      opaque().set(false);
      add(new HorizontalGroup() {{
        add(new Label() {{
          fgColor().set(new Color(155, 190, 255));
          text().set("Media System");
        }});
        anchor().set(Anchor.CENTER);
        weightY().set(0.25);
      }});
      add(new HorizontalGroup() {{
        weightX().set(1.0);
        weightY().set(1.0);
        maxWidth().set(10000);
        add(new VerticalGroup() {{
          weightX().set(0.25);
        }});
        add(new VerticalGroup() {{
          weightX().set(1.0);
          weightY().set(1.0);
        }});
        add(new VerticalGroup() {{
          weightX().set(0.25);
          add(new Button() {{
            bgColor().set(new Color(0, 0, 0, 0));
            fgColor().set(new Color(0, 0, 0, 0));
          }}); // TODO Here so something can get focus... HACK!  Need to figure out a better way
        }});
      }});
      add(new HorizontalGroup() {{
        add(new Label() {{
          fgColor().set(new Color(155, 190, 255));
          text().set("" + new Date());
        }});
        anchor().set(Anchor.CENTER);
        weightY().set(0.25);
      }});
    }};
  }
}
