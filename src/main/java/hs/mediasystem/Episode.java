package hs.mediasystem;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemProvider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.imageio.ImageIO;

public class Episode extends NamedItem {
  private final ItemProvider itemProvider;
  
  private Item item;
  
  public Episode(Item item, ItemProvider itemProvider) {
    super(item.getTitle());
    this.item = item;
    this.itemProvider = itemProvider;
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
    return null;
  }
  
  public BufferedImage getBanner() {
    return null;
  }
  
  public BufferedImage getImage() {
    ensureDataLoaded();
    
    try {
      if(item.getCover() != null) {
        return ImageIO.read(new ByteArrayInputStream(item.getCover()));
      }
    }
    catch(IOException e) {
      // Ignore
    }
    
    return new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
  }

  public String getPlot() {
    ensureDataLoaded();
    
    return item.getPlot();
  }
  
  public int getRuntime() {
    ensureDataLoaded();
    
    return item.getRuntime();
  }
  
  private void ensureDataLoaded() {
    if(item.getId() == 0) {
      try {
        if(itemProvider != null) {
          item = itemProvider.getItem(item);
        }
        else {
          item = new Item(getPath());
        }
      }
      catch(ItemNotFoundException e) {
        item = new Item(getPath());
      }
    }
  }
}
