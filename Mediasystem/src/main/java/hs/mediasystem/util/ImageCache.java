package hs.mediasystem.util;

import java.io.ByteArrayInputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import javafx.scene.image.Image;

public class ImageCache {
  private static final ReferenceQueue<Image> REFERENCE_QUEUE = new ReferenceQueue<>();
  private static final SortedMap<String, SoftReference<Image>> CACHE = Collections.synchronizedSortedMap(new TreeMap<String, SoftReference<Image>>());

  public static Image loadImage(ImageHandle handle) {
    cleanReferenceQueue();

    SoftReference<Image> refImage = CACHE.get(handle.getKey());
    Image image = refImage != null ? refImage.get() : null;

    if(image == null) {
      byte[] data = handle.getImageData();

      if(data != null) {
        image = new Image(new ByteArrayInputStream(data));

        CACHE.put(handle.getKey(), new ImageSoftReference(handle.getKey(), image, REFERENCE_QUEUE));
      }
    }

    return image;
  }

  public static Image loadImage(ImageHandle handle, double w, double h, boolean keepAspect) {
    cleanReferenceQueue();

    String key = createKey(handle.getKey(), w, h, keepAspect);
    SoftReference<Image> refImage = CACHE.get(key);
    Image image = refImage != null ? refImage.get() : null;

    if(image == null) {
      byte[] data = handle.getImageData();

      if(data != null) {
        image = new Image(new ByteArrayInputStream(data), w, h, keepAspect, true);

        CACHE.put(key, new ImageSoftReference(key, image, REFERENCE_QUEUE));
      }
    }

    return image;
  }

  private static void cleanReferenceQueue() {
    int size = CACHE.size();
    int counter = 0;


    for(;;) {
      ImageSoftReference ref = (ImageSoftReference)REFERENCE_QUEUE.poll();

      if(ref == null) {
        break;
      }

      CACHE.remove(ref.getKey());
      counter++;
    }

    if(counter > 0) {
      System.out.println("[FINE] ImageCache.cleanReferenceQueue() - Removed " + counter + "/" + size + " images.");
    }
  }

  private static String createKey(String baseKey, double w, double h, boolean keepAspect) {
    return baseKey + "#" + w + "x" + h + "-" + (keepAspect ? "T" : "F");
  }

  public static void expunge(ImageHandle handle) {
    if(handle != null) {
      String keyToRemove = handle.getKey();

      synchronized(CACHE) {
        for(Iterator<String> iterator = CACHE.tailMap(keyToRemove).keySet().iterator(); iterator.hasNext();) {
          String key = iterator.next();

          if(!key.startsWith(keyToRemove)) {
            break;
          }

          iterator.remove();
        }
      }
    }
  }

  private static class ImageSoftReference extends SoftReference<Image> {
    private final String key;

    public ImageSoftReference(String key, Image referent, ReferenceQueue<? super Image> q) {
      super(referent, q);
      this.key = key;
    }

    public String getKey() {
      return key;
    }
  }
}
