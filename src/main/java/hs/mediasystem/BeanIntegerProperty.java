package hs.mediasystem;

import hs.models.BeanAccessor;
import javafx.beans.property.SimpleIntegerProperty;

public final class BeanIntegerProperty extends SimpleIntegerProperty {
  private final BeanAccessor<Integer> accessor;

  private boolean initialized;

  public BeanIntegerProperty(Object bean, String propertyName) {
    accessor = new BeanAccessor<Integer>(bean, propertyName);
  }

  @Override
  public int get() {
    if(!initialized) {
      initialized = true;
      set(accessor.read());
    }
    
    return super.get();
  }

  @Override
  public void set(int value) {
    super.set(value);
    accessor.write(value);
  }
}