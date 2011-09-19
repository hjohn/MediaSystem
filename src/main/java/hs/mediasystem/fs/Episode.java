package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemProvider;
import hs.mediasystem.framework.MediaTree;

public class Episode extends NamedItem {
  
  public Episode(Item item, ItemProvider itemProvider) {
    super(item, itemProvider);
  }

  @Override
  public boolean isRoot() {
    return false;
  }
  
  @Override
  public MediaTree getRoot() {
    return null;
  }
}
