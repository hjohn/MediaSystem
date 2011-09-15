package hs.mediasystem.framework;

public class NoConfig implements Config<Object> {

  @Override
  public Class<?> type() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public Object copy() {
    throw new UnsupportedOperationException("Method not implemented");
  }

}
