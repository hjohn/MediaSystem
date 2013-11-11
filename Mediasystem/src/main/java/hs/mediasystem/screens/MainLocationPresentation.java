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

  public void showDialog(Dialog<?> dialog) {
    programController.showDialog(dialog);
  }
}
