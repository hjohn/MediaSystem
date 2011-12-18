package hs.mediasystem;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

public class ImageCache {
  private static Map<String, Image> cache = new HashMap<String, Image>();
  
  public static Image loadImage(ImageHandle handle) {
    Image image = cache.get(handle.getKey());

    if(image == null && handle.getImageData() != null) {
      image = new Image(new ByteArrayInputStream(handle.getImageData()));
      cache.put(handle.getKey(), image);
    }

    return image;
  }
  
  private static String createKey(String key, double w, double h, boolean keepAspect) {
    return key + "-" + w + "x" + h + "-" + (keepAspect ? "T" : "F");
  }

  public static Image loadImage(ImageHandle handle, double w, double h, boolean keepAspect) {
    String key = createKey(handle.getKey(), w, h, keepAspect);
    Image image = cache.get(key);

    if(image == null && handle.getImageData() != null) {
      image = new Image(new ByteArrayInputStream(handle.getImageData()), w, h, keepAspect, true);
      
      cache.put(key, image);
//        if(image.getWidth() == w && image.getHeight() == h) {
//          cache.put(handle.getKey(), image);
//        }
    }
    
    return image;
  }
}
