package hs.mediasystem.beans;

import javafx.beans.property.SimpleObjectProperty;

public final class BeanObjectProperty<T> extends SimpleObjectProperty<T> {
  private final BeanAccessor<T> accessor;

  private boolean initialized;

  public BeanObjectProperty(Object bean, String propertyName) {
    accessor = new BeanAccessor<T>(bean, propertyName);
  }

  @Override
  public T get() {
    if(!initialized) {
      initialized = true;
      set(accessor.read());
    }
    
    return super.get();
  }

  @Override
  public void set(T value) {
    super.set(value);
    accessor.write(value);
  }
}