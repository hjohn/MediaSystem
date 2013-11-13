package hs.mediasystem.screens;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MainLocationPresentation<L extends Location> {
  public final ObjectProperty<L> location = new SimpleObjectProperty<>();

  /**
   * Detach this presentation from any external properties.  After this call
   * the presentation is unusable and will be waiting to be garbage collected.
   */
  public void dispose() {
  }
}
