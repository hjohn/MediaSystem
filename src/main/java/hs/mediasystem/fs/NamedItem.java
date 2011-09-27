package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.LocalItem;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.GregorianCalendar;

public abstract class NamedItem implements MediaItem {
  private final MediaTree mediaTree;
  
  protected NamedItem parent;

  private LocalItem item;
  
  public NamedItem(MediaTree mediaTree, LocalItem item) {
    this.mediaTree = mediaTree;
    this.item = item;
  }
  
  public NamedItem(MediaTree mediaTree, final String title) {
    this(mediaTree, new LocalItem(null) {{
      setLocalTitle(title);
    }});
  }
  
  Item getItem() {
    return item;
  }

  public final String getTitle() {
    return item.getLocalTitle();
  }
  
  public NamedItem getParent() {
    return parent;
  }
  
  public Path getPath() {
    return item.getPath();
  }
  
  public String getSubtitle() {
    if(item.getLocalSubtitle() != null) {
      return item.getLocalSubtitle();
    }
    else {
      ensureDataLoaded();
      
      return item.getSubtitle() == null ? "" : item.getSubtitle();
    }
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

    return ImageCache.loadImage(getTitle() + "-background", item.getBackground());
  }
  
  public BufferedImage getBanner() {
    ensureDataLoaded();

    return ImageCache.loadImage(getTitle() + "-banner", item.getBanner());
  }
  
  public BufferedImage getPoster() {
    ensureDataLoaded();
    
    return ImageCache.loadImage(getTitle() + "-poster", item.getPoster());
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
    if(item.getId() == 0 && mediaTree != null) {
      System.out.println("Triggered for : " + getTitle());
      item.setId(-1);
      mediaTree.triggerItemUpdate(this);
    }
  }

  public Float getRating() {
    ensureDataLoaded();
    
    return item.getRating();
  }
}
