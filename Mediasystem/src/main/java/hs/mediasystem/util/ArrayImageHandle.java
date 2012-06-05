package hs.mediasystem.util;

public class ArrayImageHandle {
  private final byte[] imageData;
  private final String key;

  public ArrayImageHandle(byte[] imageData, String key) {
    this.imageData = imageData;
    this.key = key;
  }

  public byte[] getImageData() {
    return imageData;
  }

  public String getKey() {
    return key;
  }
}
