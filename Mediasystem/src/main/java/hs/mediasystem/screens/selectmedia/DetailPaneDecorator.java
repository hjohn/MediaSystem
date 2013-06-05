package hs.mediasystem.screens.selectmedia;

import javafx.beans.property.ObjectProperty;

public interface DetailPaneDecorator<T> {
  void decorate(boolean interactive);

  /**
   * Property for the data used for the decorated content this decorator provides.
   */
  ObjectProperty<T> dataProperty();
}
