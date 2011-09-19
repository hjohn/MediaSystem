package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.framework.MediaItem;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.imageio.ImageIO;


public abstract class NamedItem implements MediaItem {
  private final ItemProvider itemProvider;
  
  protected NamedItem parent;

  private Item item;
  
  public NamedItem(Item item, ItemProvider itemProvider) {
    this.item = item;
    this.itemProvider = itemProvider;
  }
  
  public NamedItem(final String title) {
    this(new Item(null) {{
      setTitle(title);
    }}, null);
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
  
  public String getEpisodeName() {
    ensureDataLoaded();
    
    return item.getSubtitle();
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
      try {
        if(itemProvider != null) {
          item = itemProvider.getItem(item);
        }
        else {
          item = new Item(getPath()) {{
            setTitle(NamedItem.this.getTitle());
          }};
        }
      }
      catch(ItemNotFoundException e) {
        item = new Item(getPath());
      }
    }
  }
}
