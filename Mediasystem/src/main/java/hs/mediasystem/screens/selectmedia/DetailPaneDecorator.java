package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.util.AreaPane;
import javafx.beans.property.ObjectProperty;

public interface DetailPaneDecorator<T> {
  void decorate(AreaPane areaPane);
  ObjectProperty<T> dataProperty();
}
