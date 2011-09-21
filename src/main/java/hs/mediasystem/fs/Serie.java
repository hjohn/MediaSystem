package hs.mediasystem.fs;

import hs.mediasystem.db.Item;
import hs.mediasystem.framework.MediaTree;

public class Serie extends NamedItem {
  
  public Serie(MediaTree mediaTree, Item item) {
    super(mediaTree, item);
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
