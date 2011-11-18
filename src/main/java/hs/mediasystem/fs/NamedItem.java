package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.LocalItem;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.ui.image.ImageHandle;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.GregorianCalendar;

public abstract class NamedItem implements MediaItem {
  private final MediaTree mediaTree;
  
  protected MediaItem parent;

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

  @Override
  public final String getTitle() {
    return item.getLocalTitle();
  }
  
  public MediaItem getParent() {
    return parent;
  }
  
  public Path getPath() {
    return item.getPath();
  }
  
  @Override
  public String getUri() {
    return item.getPath().toString();
  }
  
  @Override
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
    if(item.getReleaseDate() == null) {
      return null;
    }
    
    GregorianCalendar gc = new GregorianCalendar(2000, 0, 1);
    gc.setTime(item.getReleaseDate());
    return "" + gc.get(Calendar.YEAR);
  }
  
  @Override
  public int getSeason() {
    return item.getSeason();
  }
  
  @Override
  public int getEpisode() {
    return item.getEpisode();
  }
    
  public ImageHandle getBackground() {
    ensureDataLoaded();

    return item.getBackground() == null ? null : new ImageHandle(item.getBackground(), createKey("background"));
  }
  
  @Override
  public ImageHandle getBanner() {
    ensureDataLoaded();

    return item.getBanner() == null ? null : new ImageHandle(item.getBanner(), createKey("banner"));
  }
  
  @Override
  public ImageHandle getPoster() {
    ensureDataLoaded();
    
    return item.getPoster() == null ? null : new ImageHandle(item.getPoster(), createKey("poster"));
  }
  
  private String createKey(String suffix) {
    return getTitle() + "-" + getSeason() + "x" + getEpisode() + "-" + getSubtitle() + "-" + suffix;
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
