package hs.mediasystem.screens.selectmedia;

import javafx.beans.property.ObjectProperty;

public interface DetailPaneDecorator<T> {
  void decorate(boolean interactive);
  ObjectProperty<T> dataProperty();
}
