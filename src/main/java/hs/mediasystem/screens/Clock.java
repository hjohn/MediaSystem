package hs.mediasystem.screens;

import hs.mediasystem.Constants;
import hs.mediasystem.Controller;
import hs.models.Model;
import hs.models.ValueModel;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.DynamicLabel;
import hs.ui.controls.HorizontalGroup;

import java.util.Date;

public class Clock extends AbstractBlock {
  private final Model<String> currentTime = new ValueModel<String>(" ");

  @Override
  protected AbstractGroup<?> create(Controller controller) {
    new Thread() {
      {
        setDaemon(true);
      }
    
      @Override
      public void run() {
        try {
          for(;;) {
            Thread.sleep(500);
            currentTime.set(new Date().toString());
          }
        }
        catch(InterruptedException e) {
          // ignore
        }
      }
    }.start();
    
    return new HorizontalGroup() {{
      add(new DynamicLabel() {{
        fgColor().link(Constants.MAIN_TEXT_COLOR);
        text().link(currentTime);
      }});
    }};
  }
}
