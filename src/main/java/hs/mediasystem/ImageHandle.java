package hs.mediasystem;

public class ImageHandle {
  private final byte[] imageData;
  private final String key;

  public ImageHandle(byte[] imageData, String key) {
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
