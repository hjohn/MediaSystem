package hs.mediasystem.entity;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Requirement<T> {
  private final Observable observable;

  public Requirement(Observable observable) {
    this.observable = observable;
  }

  @SuppressWarnings("unchecked")
  public void attachListener(final InstanceEnricher<T, Void> enricher, final T parent) {
    ((ObservableValue<Object>)observable).addListener(new ChangeListener<Object>() {
      @Override
      public void changed(ObservableValue<? extends Object> observableValue, Object old, Object current) {
        enricher.update(parent, null);
      }
    });
  }

  @SuppressWarnings("unchecked")
  public Object getValue() {
    return ((ObservableValue<Object>)observable).getValue();
  }

  @Override
  public String toString() {
    return "Requirement(" + observable + ")";
  }
}