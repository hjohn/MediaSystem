package hs.mediasystem.beans;

import javafx.beans.property.SimpleDoubleProperty;

public final class BeanDoubleProperty extends SimpleDoubleProperty {
  private final BeanAccessor<Double> accessor;

  private boolean initialized;

  public BeanDoubleProperty(Object bean, String propertyName) {
    accessor = new BeanAccessor<Double>(bean, propertyName);
  }

  @Override
  public double get() {
    if(!initialized) {
      initialized = true;
      set(accessor.read());
    }

    return super.get();
  }

  @Override
  public void set(double value) {
    super.set(value);
    accessor.write(value);
  }
}