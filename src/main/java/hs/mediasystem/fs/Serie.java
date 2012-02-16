package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.nio.file.Paths;
import java.util.List;

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
      children = new EpisodeScanner(getMediaTree(), new EpisodeDecoder(), "EPISODE").scan(Paths.get(getLocalInfo().getUri()));
    }

    return children;
  }
}
