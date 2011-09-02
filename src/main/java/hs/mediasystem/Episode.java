package hs.mediasystem;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.screens.movie.Element;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public class Episode extends NamedItem {
  protected EpisodeGroup episodeGroup;

  private final Element element;
  private final ItemProvider itemProvider;
  
  private Item item;
  
  public Episode(Element element, ItemProvider itemProvider) {
    super(element.getTitle());
    this.element = element;
    this.itemProvider = itemProvider;
  }

  public Path getPath() {
    return element.getPath();
  }
  
  public String getSubtitle() {
    return element.getSubtitle();
  }
  
  public String getYear() {
    return element.getYear();
  }
  
  public int getSequence() {
    return element.getSequence();
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
    if(item == null) {
      try {
        if(itemProvider != null) {
          item = itemProvider.getItem(element);
        }
        else {
          item = new Item();
        }
      }
      catch(ItemNotFoundException e) {
        item = new Item();
      }
    }
  }
  
  public EpisodeGroup getEpisodeGroup() {
    return episodeGroup;
  }
}
