package hs.mediasystem.fs;

import java.util.List;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

public class Serie extends MediaItem {
  private List<? extends MediaItem> children;

  public Serie(MediaTree mediaTree, LocalInfo localInfo) {
    super(mediaTree, localInfo);
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public List<? extends MediaItem> children() {
    if(children == null) {
      children = new EpisodeScanner(getMediaTree(), new EpisodeDecoder(), MediaType.EPISODE).scan(getLocalInfo().getPath());
    }

    return children;
  }
}
