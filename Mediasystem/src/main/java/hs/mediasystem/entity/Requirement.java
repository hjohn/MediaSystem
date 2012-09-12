package hs.mediasystem.entity;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class Requirement<T> {
  private final Observable observable;
  private final Object key;

  public Requirement(Observable observable, Object key) {
    this.observable = observable;
    this.key = key;
  }

  public Requirement(Observable observable) {
    this(observable, null);
  }

  @SuppressWarnings("unchecked")
  public void attachListener(final InstanceEnricher<T, Void> enricher, final T parent) {
    if(observable instanceof ObservableMap) {
      ((ObservableMap<Object, Object>)observable).addListener(new MapChangeListener<Object, Object>() {
        @Override
        public void onChanged(MapChangeListener.Change<? extends Object, ? extends Object> change) {
          if(change.wasAdded() && change.getKey().equals(key)) {
            enricher.update(parent, null);
          }
        }
      });
    }
    else {
      ((ObservableValue<Object>)observable).addListener(new ChangeListener<Object>() {
        @Override
        public void changed(ObservableValue<? extends Object> observableValue, Object old, Object current) {
          enricher.update(parent, null);
        }
      });
    }
  }

  @SuppressWarnings("unchecked")
  public Object getValue() {
    if(observable instanceof ObservableMap) {
      return ((ObservableMap<Object, Object>)observable).get(key);
    }

    return ((ObservableValue<Object>)observable).getValue();
  }

  @Override
  public String toString() {
    return "Requirement(" + observable + ")";
  }
}