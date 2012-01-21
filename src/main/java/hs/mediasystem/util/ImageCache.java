package hs.mediasystem.util;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

public class ImageCache {
  private static final Map<String, Map<String, Image>> CACHE = new HashMap<>();

  public static Image loadImage(ImageHandle handle) {
    Map<String, Image> map = CACHE.get(handle.getKey());
    Image image = null;

    if(map != null) {
      image = map.get("original");
    }

    if(image == null && handle.getImageData() != null) {
      image = new Image(new ByteArrayInputStream(handle.getImageData()));

      if(map == null) {
        map = new HashMap<>();
        CACHE.put(handle.getKey(), map);
      }

      map.put("original", image);
    }

    return image;
  }

  public static Image loadImage(ImageHandle handle, double w, double h, boolean keepAspect) {
    String key = createKey(w, h, keepAspect);
    Map<String, Image> map = CACHE.get(handle.getKey());
    Image image = null;

    if(map != null) {
      image = map.get(key);
    }

    if(image == null && handle.getImageData() != null) {
      image = new Image(new ByteArrayInputStream(handle.getImageData()), w, h, keepAspect, true);

      if(map == null) {
        map = new HashMap<>();
        CACHE.put(handle.getKey(), map);
      }

      map.put(key, image);
    }

    return image;
  }

  private static String createKey(double w, double h, boolean keepAspect) {
    return w + "x" + h + "-" + (keepAspect ? "T" : "F");
  }

  public static void expunge(ImageHandle handle) {
    if(handle != null) {
      CACHE.remove(handle.getKey());
    }
  }
}
