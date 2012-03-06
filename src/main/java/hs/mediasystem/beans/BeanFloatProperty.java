package hs.mediasystem.beans;

import javafx.beans.property.SimpleFloatProperty;

public final class BeanFloatProperty extends SimpleFloatProperty {
  private final Accessor<Float> accessor;

  private boolean initialized;

  public BeanFloatProperty(Object bean, String propertyName) {
    accessor = new BeanAccessor<>(bean, propertyName);
  }

  public BeanFloatProperty(Accessor<Float> accessor) {
    this.accessor = accessor;
  }

  @Override
  public float get() {
    if(!initialized) {
      initialized = true;
      super.set(accessor.read());
    }

    return super.get();
  }

  @Override
  public void set(float value) {
    super.set(value);
    accessor.write(value);
  }
}