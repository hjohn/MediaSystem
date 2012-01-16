package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaTree;

public class Episode extends NamedItem {

  public Episode(LocalInfo localInfo) {
    super(localInfo);
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
