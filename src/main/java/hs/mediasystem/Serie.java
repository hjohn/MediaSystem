package hs.mediasystem;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemProvider;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Serie extends NamedItem {
  private final ItemProvider serieProvider;
  
  private final Item item;
  
  public Serie(Item item, ItemProvider serieProvider) {
    super(item.getTitle());
    this.item = item;
    this.serieProvider = serieProvider;
  }
  
  @Override
  public boolean isRoot() {
    return true;
  }
  
  @Override
  public MediaTree getRoot() {
    return new EpisodesMediaTree(item.getPath(), this);
  }
  
  public BufferedImage getBanner() {
    ensureDataLoaded();
    
    try {
      if(record.getCover() != null) {
        return ImageIO.read(new ByteArrayInputStream(record.getCover()));
      }
    }
    catch(IOException e) {
      // Ignore
    }
    
    return new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
  }
  
  public String getProvider() {
    ensureDataLoaded();
    
    return record.getProvider();
  }
  
  public String getProviderId() {
    ensureDataLoaded();
    
    return record.getProviderId();
  }
  
  private Item record;
  
  private void ensureDataLoaded() {
    if(record == null) {
      try {
        if(serieProvider != null) {
          record = serieProvider.getItem(item);
        }
        else {
          record = new Item(item.getPath());
        }
      }
      catch(ItemNotFoundException e) {
        record = new Item(item.getPath());
      }
    }
  }
}
