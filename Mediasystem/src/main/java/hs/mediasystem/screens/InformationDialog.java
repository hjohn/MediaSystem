package hs.mediasystem.screens;

import hs.mediasystem.util.DialogPane;
import javafx.scene.control.Label;

public class InformationDialog extends DialogPane<Void> {
  public InformationDialog(String text) {
    getChildren().add(new Label(text));
  }
}
