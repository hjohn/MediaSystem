package hs.mediasystem;

public interface Grouper<T> {
  Object getGroup(T item);
}
