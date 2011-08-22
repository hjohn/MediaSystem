package hs.mediasystem.screens;

import hs.mediasystem.Constants;
import hs.mediasystem.Controller;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.DynamicLabel;
import hs.ui.controls.HorizontalGroup;

public class Header extends AbstractBlock {

  @Override
  protected AbstractGroup<?> create(Controller controller) {
    return new HorizontalGroup() {{
      weightX().set(1.0);
      add(new DynamicLabel() {{
        fgColor().link(Constants.MAIN_TEXT_COLOR);
        font().link(Constants.HEADER_FONT);
        text().set("Media System v0.1");
      }});
    }};
  }
}
