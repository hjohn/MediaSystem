package hs.mediasystem.util;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

public class WeakBinder {
  private final List<Object> hardRefs = new ArrayList<>();

  public <T> void bind(final Property<T> property, final ObservableValue<? extends T> dest) {
    InvalidationListener invalidationListener = new InvalidationListener() {
      @Override
      public void invalidated(Observable observable) {
        property.setValue(dest.getValue());
      }
    };

    dest.addListener(new WeakInvalidationListener(invalidationListener));
    property.setValue(dest.getValue());

    hardRefs.add(dest);
    hardRefs.add(invalidationListener);
  }
}
