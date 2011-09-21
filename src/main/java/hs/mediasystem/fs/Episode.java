package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.framework.MediaTree;

public class Episode extends NamedItem {
  
  public Episode(MediaTree mediaTree, Item item) {
    super(mediaTree, item);
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
