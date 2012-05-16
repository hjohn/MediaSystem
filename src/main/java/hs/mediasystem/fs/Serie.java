package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Serie extends MediaItem {
  private List<MediaItem> children;

  public Serie(MediaTree mediaTree, LocalInfo<?> localInfo) {
    super(mediaTree, localInfo, true);
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public List<? extends MediaItem> children() {
    if(children == null) {
      List<LocalInfo<Object>> scanResults = new EpisodeScanner(new EpisodeDecoder(getTitle()), "EPISODE").scan(Paths.get(getLocalInfo().getUri()));

      children = new ArrayList<>();

      for(LocalInfo<Object> localInfo : scanResults) {
        children.add(new MediaItem(this, localInfo));
      }
    }

    return children;
  }
}
