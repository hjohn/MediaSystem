package hs.mediasystem.beans;

public class FloatToIntAccessor implements Accessor<Integer> {
  private final Accessor<Float> accessor;
  private final float factor;

  public FloatToIntAccessor(Accessor<Float> accessor, float factor) {
    this.accessor = accessor;
    this.factor = factor;
  }

  @Override
  public Integer read() {
    Float f = accessor.read();

    return f == null ? null : (int)(f * factor);
  }

  @Override
  public void write(Integer value) {
    accessor.write(value == null ? null : (float)value / factor);
  }
}
