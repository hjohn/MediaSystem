package hs.mediasystem.beans;

public interface Accessor<T> {
  T read();
  void write(T value);
}
