package hs.mediasystem.screens;

import hs.mediasystem.util.Dialog;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MainLocationPresentation {
  public final ObjectProperty<Location> location = new SimpleObjectProperty<>();

  private final ProgramController programController;

  public MainLocationPresentation(ProgramController programController) {
    this.programController = programController;
  }

  public <R> R showSynchronousDialog(Dialog<R> dialog) {
    return programController.showSynchronousDialog(dialog);
  }

  // TODO temporary
  public ProgramController getProgramController() {
    return programController;
  }

  // TODO temporary?  here to be able to dispose of bindings to Player as Player is such a long lived object...
  protected void dispose() {
  }
}
