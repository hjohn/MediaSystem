package hs.mediasystem.beans;

import javafx.beans.property.SimpleLongProperty;

public final class ComplexLongProperty extends SimpleLongProperty {
  private final Accessor<Long> accessor;

  private boolean initialized;

  public ComplexLongProperty(Accessor<Long> accessor) {
    this.accessor = accessor;
  }

  @Override
  public long get() {
    if(!initialized) {
      initialized = true;
      super.set(accessor.read());
    }

    return super.get();
  }

  @Override
  public void set(long value) {
    super.set(value);
    accessor.write(value);
  }
}