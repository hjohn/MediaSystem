package hs.mediasystem.fs;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class SeriesMediaTree extends AbstractMediaTree {
  private final Path root;

  private List<? extends MediaItem> children;

  public SeriesMediaTree(Path root) {
    this.root = root;
  }

  @Override
  public List<? extends MediaItem> children() {
    if(children == null) {
      children = new SerieScanner(this).scan(root);
      Collections.sort(children, MediaItemComparator.INSTANCE);
    }

    return children;
  }

  @Override
  public MediaTree parent() {
    return null;
  }
}
