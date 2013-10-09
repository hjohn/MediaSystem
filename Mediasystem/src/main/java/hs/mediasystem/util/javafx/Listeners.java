package hs.mediasystem.util.javafx;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Listeners {

  // TODO the method names as well as the class name are somewhat inaccurate at describing the purpose of this collection of helper methods

  /**
   * Binds an ObservableValue with the use of a ChangeListener.  The ChangeListener is called
   * immediately after adding it to make sure the initial value of the ObservableValue is acted
   * upon.
   *
   * @param observableValue an ObservableValue instance
   * @param changeListener a ChangeListener
   */
  public static <T> void bind(ObservableValue<T> observableValue, ChangeListener<T> changeListener) {
    observableValue.addListener(changeListener);

    changeListener.changed(observableValue, observableValue.getValue(), observableValue.getValue());
  }
}
