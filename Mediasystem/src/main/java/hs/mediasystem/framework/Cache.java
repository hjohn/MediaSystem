package hs.mediasystem.framework;

public interface Cache {
  byte[] lookup(String key);
  void store(String key, byte[] data);
}
