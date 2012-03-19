package hs.mediasystem.beans;

public class IntToLongAccessor implements Accessor<Long> {
  private final Accessor<Integer> accessor;

  public IntToLongAccessor(Accessor<Integer> accessor) {
    this.accessor = accessor;
  }

  @Override
  public Long read() {
    Integer integer = accessor.read();

    return integer == null ? null : integer.longValue();
  }

  @Override
  public void write(Long value) {
    accessor.write(value == null ? null : value.intValue());
  }
}
