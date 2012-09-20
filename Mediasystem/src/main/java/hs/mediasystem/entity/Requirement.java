package hs.mediasystem.entity;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Requirement<T> {
  private final Property<Object> property;

  public Requirement(Property<Object> property) {
    this.property = property;
  }

  public void attachListener(final InstanceEnricher<T, Void> enricher, final T parent) {
    property.addListener(new ChangeListener<Object>() {
      @Override
      public void changed(ObservableValue<? extends Object> observableValue, Object old, Object current) {
        enricher.update(parent, null);
      }
    });
  }

  public Object getValue() {
    return property.getValue();
  }

  @Override
  public String toString() {
    return "Requirement(" + property.getName() + ")";
  }
}