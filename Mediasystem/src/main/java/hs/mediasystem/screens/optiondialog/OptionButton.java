package hs.mediasystem.screens.optiondialog;

import javafx.scene.Node;
import javafx.scene.control.Button;

public class OptionButton extends Button {

  public OptionButton(String text, Node graphic) {
    super(text, graphic);

    getStyleClass().add("option");
    setMaxWidth(Double.MAX_VALUE);
  }

  public OptionButton(String text) {
    this(text, null);
  }

  public OptionButton() {
    this(null, null);
  }
}
