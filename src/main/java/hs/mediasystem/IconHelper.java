package hs.mediasystem;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class IconHelper {
  public static ImageIcon readIcon(String location) {
    try {
      BufferedImage image = ImageIO.read(new File(location));
      return new ImageIcon(image);
    }
    catch(IIOException e) {
      System.err.println("[WARNING] Could not read icon at \"" + location + "\": " + e);
    }
    catch(IOException e) {
      System.err.println("[WARNING] Could not read icon at \"" + location + "\": " + e);
    }
    
    return null;
  }
  
  public static ImageIcon readIcon(String location, int width, int height) {
    try {
      BufferedImage image = ImageIO.read(new File(location));
      image = resize(image, width, height);
      return new ImageIcon(image);
    }
    catch(IIOException e) {
      System.err.println("[WARNING] Could not read icon at \"" + location + "\": " + e);
    }
    catch(IOException e) {
      System.err.println("[WARNING] Could not read icon at \"" + location + "\": " + e);
    }
    
    return null;
  }
  
  public static BufferedImage resize2(BufferedImage image, int newW, int newH) {
    System.out.println(image.getType());
    BufferedImage scaledImage = new BufferedImage(newW, newH, image.getType());
    Graphics2D g = scaledImage.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(image, 0, 0, newW, newH, 0, 0, image.getWidth(), image.getHeight(), null);
    g.dispose();
    return scaledImage;
  }
  
  public static BufferedImage resize(BufferedImage source, int newW, int newH) {
    BufferedImage target = new BufferedImage(newW, newH, source.getType());
    
    double scalex = (double) target.getWidth() / source.getWidth();
    double scaley = (double) target.getHeight() / source.getHeight();
    AffineTransform at = AffineTransform.getScaleInstance(scalex, scaley);

    AffineTransformOp affineTransformOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);

    affineTransformOp.filter(source, target);
    
    return target;
  }
}
