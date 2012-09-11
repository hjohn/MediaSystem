package hs.mediasystem.framework;

public interface EntityProvider<T> {
  T get(Object... parameters);
}
