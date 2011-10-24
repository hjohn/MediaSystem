package hs.mediasystem.fs;

import hs.mediasystem.db.LocalItem;
import hs.mediasystem.framework.MediaTree;

public class Serie extends NamedItem {
  
  public Serie(MediaTree mediaTree, LocalItem item) {
    super(mediaTree, item);
  }
  
  @Override
  public boolean isRoot() {
    return true;
  }
  
  @Override
  public boolean isLeaf() {
    return false;
  }
  
  @Override
  public MediaTree getRoot() {
    return new EpisodesMediaTree(getPath(), this);
  }
}
