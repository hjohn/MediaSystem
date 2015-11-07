package hs.mediasystem.framework;

public interface Cache<T> {
  CacheEntry<T> lookup(String key);
  void store(String key, T data);
}
