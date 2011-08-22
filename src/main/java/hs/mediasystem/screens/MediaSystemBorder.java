package hs.mediasystem.screens;

import hs.mediasystem.Controller;
import hs.ui.controls.AbstractGroup;
import hs.ui.controls.VerticalGroup;

public class MediaSystemBorder extends AbstractBlock {
  
  // Make methods to assign new values to Extensions
  // Keep track of extensions when created a piece of UI
  
  /*
   * Clock
   * Copyright
   * MainOptions
   * Border[left, center, right]
   * 
   * MainMenu = Border[Clock, MainOptions, Copyright]
   * MovieSelection = Border[Clock, MovieList, Copyright]
   *
   * new Border(new Configuration(clock, new MainOptions(), copyright))
   * 
   *
   *
   *
   */
  
  @Override
  protected AbstractGroup<?> create(final Controller controller) {
    return new VerticalGroup() {{
      overrideWeightX(1.0);
      overrideWeightY(1.0);
      opaque().set(false);

      addExtension(this, "top");
      addExtension(this, "center");
      addExtension(this, "bottom");
    }};
  }
}
