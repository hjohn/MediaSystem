package hs.mediasystem.util;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

public class ImageCache {
  private static final Map<String, Image> CACHE = new HashMap<>();

  public static Image loadImage(ImageHandle handle) {
    Image image = CACHE.get(handle.getKey());

    if(image == null && handle.getImageData() != null) {
      image = new Image(new ByteArrayInputStream(handle.getImageData()));
      CACHE.put(handle.getKey(), image);
    }

    return image;
  }

  private static String createKey(String key, double w, double h, boolean keepAspect) {
    return key + "-" + w + "x" + h + "-" + (keepAspect ? "T" : "F");
  }

  public static Image loadImage(ImageHandle handle, double w, double h, boolean keepAspect) {
    String key = createKey(handle.getKey(), w, h, keepAspect);
    Image image = CACHE.get(key);

    if(image == null && handle.getImageData() != null) {
      image = new Image(new ByteArrayInputStream(handle.getImageData()), w, h, keepAspect, true);

      CACHE.put(key, image);
//        if(image.getWidth() == w && image.getHeight() == h) {
//          cache.put(handle.getKey(), image);
//        }
    }

    return image;
  }
}
