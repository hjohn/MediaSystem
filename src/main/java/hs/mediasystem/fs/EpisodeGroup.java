package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.Group;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EpisodeGroup extends MediaItem implements Group {
  private final List<MediaItem> children = new ArrayList<>();

  public EpisodeGroup(MediaTree mediaTree, List<MediaItem> items) {
    super(mediaTree, new LocalInfo(MediaType.MOVIE, items.get(0).getTitle(), items.get(0).getReleaseYear()));

    for(MediaItem item : items) {
      add(item);
    }
  }

  public void add(MediaItem child) {
    children.add(child);
    child.setParent(this);
  }

  public int size() {
    return children.size();
  }

  @Override
  public Collection<? extends MediaItem> children() {
    return Collections.unmodifiableCollection(children);
  }

  @Override
  public boolean isRoot() {
    return false;
  }

  @Override
  public boolean isLeaf() {
    return false;
  }

  @Override
  public MediaTree getRoot() {
    return null;
  }
}
