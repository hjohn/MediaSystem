package hs.mediasystem.entity;

public interface EntityProvider<K, T> {
  T get(K key);
}
