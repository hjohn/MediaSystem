package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Season extends MediaItem {
  private final List<MediaItem> children = new ArrayList<>();

  public Season(MediaTree mediaTree, String serieName, int season) {
    super(mediaTree, new LocalInfo<>(null, "SEASON", null, serieName, null, null, null, season, null, null));
  }

  public void add(MediaItem child) {
    children.add(child);
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
