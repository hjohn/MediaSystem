package hs.mediasystem.framework.actions;

import hs.mediasystem.util.StringConverter;
import javafx.scene.control.ListCell;

// TODO move to more general package
public class StringConvertingCell<T> extends ListCell<T> {
  private final StringConverter<T> stringConverter;

  public StringConvertingCell(StringConverter<T> stringConverter) {
    this.stringConverter = stringConverter;
  }

  @Override
  public void updateItem(T item, boolean empty) {
    super.updateItem(item, empty);

    if(item != null) {
      setText(stringConverter.toString(item));
    }
    else {
      setText(null);
    }
  }
}