package hs.mediasystem.screens;

import hs.mediasystem.util.DialogPane;
import javafx.scene.control.Label;

public class InformationDialog extends DialogPane<Void> {
  public InformationDialog(String text) {
    Label label = new Label(text);

    label.setFocusTraversable(true);
    label.setStyle(".initial-focus");

    getChildren().add(label);
  }
}
