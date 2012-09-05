package hs.mediasystem.screens.selectmedia;

import javafx.beans.property.ObjectProperty;

public interface DetailPaneDecorator<T> {
  void decorate();
  ObjectProperty<T> dataProperty();
}
