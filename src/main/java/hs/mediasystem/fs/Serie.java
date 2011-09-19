package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.framework.MediaTree;

public class Serie extends NamedItem {
  
  public Serie(Item item, ItemProvider serieProvider) {
    super(item, serieProvider);
  }
  
  @Override
  public boolean isRoot() {
    return true;
  }
  
  @Override
  public MediaTree getRoot() {
    return new EpisodesMediaTree(getPath(), this);
  }
}
