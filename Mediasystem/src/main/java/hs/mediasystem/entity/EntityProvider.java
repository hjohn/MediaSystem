package hs.mediasystem.entity;

public interface EntityProvider<T> {
  T get(Object... parameters);
}
