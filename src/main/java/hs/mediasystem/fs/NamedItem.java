package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.imageio.ImageIO;

public abstract class NamedItem implements MediaItem {
  private final MediaTree mediaTree;
  
  protected NamedItem parent;

  private Item item;
  
  public NamedItem(MediaTree mediaTree, Item item) {
    this.mediaTree = mediaTree;
    this.item = item;
  }
  
  public NamedItem(MediaTree mediaTree, final String title) {
    this(mediaTree, new Item(null) {{
      setTitle(title);
    }});
  }
  
  Item getItem() {
    return item;
  }
  
  void setItem(Item item) {
    this.item = item;
  }
  
  public final String getTitle() {
    return item.getTitle();
  }
  
  public NamedItem getParent() {
    return parent;
  }
  
  public Path getPath() {
    return item.getPath();
  }
  
  public String getSubtitle() {
    ensureDataLoaded();
    
    return item.getSubtitle() == null ? "" : item.getSubtitle();
  }
  
  public String getYear() {
    GregorianCalendar gc = new GregorianCalendar(2000, 0, 1);
    gc.setTime(item.getReleaseDate());
    return "" + gc.get(Calendar.YEAR);
  }
  
  public int getSeason() {
    return item.getSeason();
  }
  
  public int getEpisode() {
    return item.getEpisode();
  }
    
  public BufferedImage getBackground() {
    ensureDataLoaded();

    return loadImage(item.getBackground());
  }
  
  public BufferedImage getBanner() {
    ensureDataLoaded();

    return loadImage(item.getBanner());
  }
  
  public BufferedImage getPoster() {
    ensureDataLoaded();
    
    return loadImage(item.getPoster());
  }
  
  private static BufferedImage loadImage(byte[] stream) {
    try {
      if(stream != null) {
        return ImageIO.read(new ByteArrayInputStream(stream));
      }
    }
    catch(IOException e) {
      // Ignore
    }
    
    return null;
  }

  public String getPlot() {
    ensureDataLoaded();
    
    return item.getPlot();
  }
  
  public int getRuntime() {
    ensureDataLoaded();
    
    return item.getRuntime();
  }
  
  public String getProvider() {
    ensureDataLoaded();
    
    return item.getProvider();
  }
  
  public String getProviderId() {
    ensureDataLoaded();
    
    return item.getProviderId();
  }
  
  private void ensureDataLoaded() {  // TODO should only enrich, not completely replace
    if(item.getId() == 0) {
      item.setId(-1);
      mediaTree.triggerItemUpdate(this);
    }
  }

  public Float getRating() {
    return item.getRating();
  }
}
