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

public class Season extends NamedItem implements Group {
  private final List<MediaItem> children = new ArrayList<>();

  public Season(String title, int season) {
    super(new LocalInfo(null, MediaType.EPISODE, title, null, null, null, season, null));
  }

  public void add(MediaItem child) {
    children.add(child);
    ((NamedItem)child).parent = this;
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
