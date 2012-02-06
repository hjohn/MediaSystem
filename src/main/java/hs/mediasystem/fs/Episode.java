package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

public class Episode extends MediaItem {

  public Episode(MediaTree mediaTree, LocalInfo localInfo) {
    super(mediaTree, localInfo);
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
