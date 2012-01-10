package hs.mediasystem.fs;

import hs.mediasystem.db.LocalItem;
import hs.mediasystem.framework.MediaTree;

public class Episode extends NamedItem {

  public Episode(MediaTree mediaTree, LocalItem item, String type) {
    super(mediaTree, item, type);
  }

  @Override
  public boolean isRoot() {
    return false;
  }

  @Override
  public boolean isLeaf() {
    return true;
  }

  @Override
  public MediaTree getRoot() {
    return null;
  }
}
