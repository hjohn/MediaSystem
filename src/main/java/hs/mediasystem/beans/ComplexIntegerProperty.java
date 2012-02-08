package hs.mediasystem.beans;

import javafx.beans.property.SimpleIntegerProperty;

public final class ComplexIntegerProperty extends SimpleIntegerProperty {
  private final Accessor<Integer> accessor;

  private boolean initialized;

  public ComplexIntegerProperty(Accessor<Integer> accessor) {
    this.accessor = accessor;
  }

  @Override
  public int get() {
    if(!initialized) {
      initialized = true;
      super.set(accessor.read());
    }

    return super.get();
  }

  @Override
  public void set(int value) {
    super.set(value);
    accessor.write(value);
  }
}