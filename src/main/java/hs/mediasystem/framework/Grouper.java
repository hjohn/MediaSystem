package hs.mediasystem.framework;

public interface Grouper<T> {
  Object getGroup(T item);
}
