package hs.mediasystem.screens;

import hs.mediasystem.util.Dialog;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MainLocationPresentation<L extends Location> {
  public final ObjectProperty<L> location = new SimpleObjectProperty<>();

  private final ProgramController programController;

  public MainLocationPresentation(ProgramController programController) {
    this.programController = programController;
  }

  /**
   * Detach this presentation from any external properties.  After this call
   * the presentation is unusable and will be waiting to be garbage collected.
   */
  public void dispose() {
  }

  public <R> R showSynchronousDialog(Dialog<R> dialog) {
    return programController.showSynchronousDialog(dialog);
  }

  public void showDialog(Dialog<?> dialog) {
    programController.showDialog(dialog);
  }
}
