package hs.mediasystem.fs;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageCache {
  private static Map<String, BufferedImage> cache = new HashMap<String, BufferedImage>();
  
  public static BufferedImage loadImage(String name, byte[] imageData) {
    BufferedImage image = cache.get(name);
    
    if(image == null && imageData != null) {
      try {
        image = ImageIO.read(new ByteArrayInputStream(imageData));
        cache.put(name, image);
      }
      catch(IOException e) {
        // Ignore
      }
    }
    
    return image;
  }
}
