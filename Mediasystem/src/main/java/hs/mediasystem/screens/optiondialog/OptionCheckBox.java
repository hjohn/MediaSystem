package hs.mediasystem.screens.optiondialog;

import javafx.scene.control.CheckBox;

public class OptionCheckBox extends CheckBox {

  public OptionCheckBox(String text) {
    super(text);

    getStyleClass().add("option");
    setMaxWidth(Double.MAX_VALUE);
  }

  public OptionCheckBox() {
    this(null);
  }
}
