package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EpisodeGroup extends MediaItem {
  private final List<MediaItem> children = new ArrayList<>();

  public EpisodeGroup(MediaTree mediaTree, List<MediaItem> items) {
    super(mediaTree, new LocalInfo<>("EpisodeGroup://" + items.get(0).getTitle(), "MOVIE", items.get(0).getTitle(), items.get(0).getReleaseYear()));

    children.addAll(items);

    setEnriched();
  }

  @Override
  public List<? extends MediaItem> children() {
    return Collections.unmodifiableList(children);
  }

  @Override
  public boolean isLeaf() {
    return false;
  }
}
