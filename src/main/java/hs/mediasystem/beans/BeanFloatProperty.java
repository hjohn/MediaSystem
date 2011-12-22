package hs.mediasystem.beans;

import javafx.beans.property.SimpleFloatProperty;

public final class BeanFloatProperty extends SimpleFloatProperty {
  private final BeanAccessor<Float> accessor;

  private boolean initialized;

  public BeanFloatProperty(Object bean, String propertyName) {
    accessor = new BeanAccessor<Float>(bean, propertyName);
  }

  @Override
  public float get() {
    if(!initialized) {
      initialized = true;
      set(accessor.read());
    }
    
    return super.get();
  }

  @Override
  public void set(float value) {
    super.set(value);
    accessor.write(value);
  }
}