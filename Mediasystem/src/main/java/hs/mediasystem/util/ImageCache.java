package hs.mediasystem.util;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

public class ImageCache {
  private static final ReferenceQueue<Image> REFERENCE_QUEUE = new ReferenceQueue<>();
  private static final SortedMap<String, WeakReference<Image>> CACHE = Collections.synchronizedSortedMap(new TreeMap<String, WeakReference<Image>>());

  public static Image loadImage(ImageHandle handle) {
    cleanReferenceQueue();

    WeakReference<Image> refImage = CACHE.get(handle.getKey());
    Image image = refImage != null ? refImage.get() : null;

    if(image == null) {
      byte[] data = handle.getImageData();

      if(data != null) {
        image = new Image(new ByteArrayInputStream(data));

        store(handle.getKey(), image);
      }
    }

    return image;
  }

  public static Image loadImage(ImageHandle handle, double w, double h, boolean keepAspect) {
    cleanReferenceQueue();

    String key = createKey(handle.getKey(), w, h, keepAspect);
    WeakReference<Image> refImage = CACHE.get(key);
    Image image = refImage != null ? refImage.get() : null;

    if(image == null) {
      byte[] data = handle.getImageData();

      if(data != null) {
        image = new Image(new ByteArrayInputStream(data), w, h, keepAspect, true);

        store(key, image);
      }
    }

    return image;
  }

  public static Image loadImageUptoMaxSize(ImageHandle handle, int w, int h) {
    cleanReferenceQueue();

    String key = createKey(handle.getKey(), w, h, true);
    WeakReference<Image> refImage = CACHE.get(key);
    Image image = refImage != null ? refImage.get() : null;

    if(image == null) {
      byte[] data = handle.getImageData();

      if(data != null) {
        Dimension size = determineSize(data);

        if(size != null) {
          if(size.width <= w && size.height <= h) {
            image = new Image(new ByteArrayInputStream(data));
          }
          else {
            image = new Image(new ByteArrayInputStream(data), w, h, true, true);
          }

          store(key, image);
        }
      }
    }

    return image;
  }

  private static Dimension determineSize(byte[] data) {
    try(ImageInputStream is = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
      Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(is);

      if(imageReaders.hasNext()) {
        ImageReader imageReader = imageReaders.next();

        imageReader.setInput(is);

        int w = imageReader.getWidth(imageReader.getMinIndex());
        int h = imageReader.getHeight(imageReader.getMinIndex());

        imageReader.dispose();

        return new Dimension(w, h);
      }

      return null;
    }
    catch(IOException e) {
      return null;
    }
  }

  private static void store(String key, Image image) {
    ImageWeakReference imageRef = new ImageWeakReference(key, image, REFERENCE_QUEUE);

    CACHE.put(key, imageRef);
  }

  private static void cleanReferenceQueue() {
    int size = CACHE.size();
    int counter = 0;

    for(;;) {
      ImageWeakReference ref = (ImageWeakReference)REFERENCE_QUEUE.poll();

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
        for(Iterator<Map.Entry<String, WeakReference<Image>>> iterator = CACHE.tailMap(keyToRemove).entrySet().iterator(); iterator.hasNext();) {
          Map.Entry<String, WeakReference<Image>> entry = iterator.next();

          if(!entry.getKey().startsWith(keyToRemove)) {
            break;
          }

          iterator.remove();
        }
      }
    }
  }

  private static class ImageWeakReference extends WeakReference<Image> {
    private final String key;

    public ImageWeakReference(String key, Image referent, ReferenceQueue<? super Image> q) {
      super(referent, q);
      this.key = key;
    }

    public String getKey() {
      return key;
    }
  }
}
