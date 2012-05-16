package hs.mediasystem.screens.optiondialog;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;

public class BooleanOption extends Option {
  private final BooleanProperty property;

  public BooleanOption(String description, final BooleanProperty property, final StringBinding binding) {
    super(description);
    this.property = property;

    label.textProperty().bind(binding);
  }

  @Override
  public void left() {
    property.set(!property.get());
  }

  @Override
  public void right() {
    property.set(!property.get());
  }
}