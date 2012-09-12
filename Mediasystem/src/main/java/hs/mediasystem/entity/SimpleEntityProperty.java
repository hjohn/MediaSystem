package hs.mediasystem.entity;

import javafx.beans.property.SimpleObjectProperty;

public class SimpleEntityProperty<T> extends SimpleObjectProperty<T> {
  private InstanceEnricher<?, ?> enricher;

  public SimpleEntityProperty(Object bean, String name) {
    super(bean, name);
  }

  public <E> void setEnricher(InstanceEnricher<E, ?> enricher) {
    this.enricher = enricher;
  }

  @SuppressWarnings("unchecked")
  public <E> InstanceEnricher<E, T> getEnricher() {
    return (InstanceEnricher<E, T>)enricher;
  }
}
