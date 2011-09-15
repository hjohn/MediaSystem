package hs.mediasystem.framework;

public interface Config<T> {
  Class<?> type();
  T copy();
}
