package hs.mediasystem.util;

import javafx.beans.Observable;

public abstract class StringBinding extends javafx.beans.binding.StringBinding {
  public StringBinding(Observable... observables) {
    bind(observables);
  }
}
