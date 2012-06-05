package hs.mediasystem.screens.optiondialog;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

public class ListOption<T> extends Option {
  private final ObjectProperty<T> property;
  private final ObservableList<T> items;

  public ListOption(String description, final ObjectProperty<T> property, ObservableList<T> items, final StringBinding binding) {
    super(description);
    this.property = property;
    this.items = items;

    label.textProperty().bind(binding);
  }

  @Override
  public void left() {
    int index = items.indexOf(property.get()) - 1;

    if(index < 0) {
      index = items.size() - 1;
    }

    property.set(items.get(index));
  }

  @Override
  public void right() {
    int index = items.indexOf(property.get()) + 1;

    if(index >= items.size()) {
      index = 0;
    }

    property.set(items.get(index));
  }

}