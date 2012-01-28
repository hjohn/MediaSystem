package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaTree;

public class Serie extends NamedItem {

  public Serie(LocalInfo localInfo) {
    super(localInfo);
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
    return new EpisodesMediaTree(getPath(), getTitle());
  }
}
