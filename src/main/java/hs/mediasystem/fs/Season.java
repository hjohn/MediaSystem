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

public class Season extends MediaItem implements Group {
  private final List<MediaItem> children = new ArrayList<>();

  public Season(MediaTree mediaTree, String serieName, int season) {
    super(mediaTree, new LocalInfo(null, MediaType.SEASON, serieName, null, null, null, season, null));
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
