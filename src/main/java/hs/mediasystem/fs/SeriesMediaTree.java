package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;

import java.nio.file.Path;
import java.util.List;

public class SeriesMediaTree extends AbstractMediaTree {
  private final Path root;

  private List<? extends MediaItem> children;

  public SeriesMediaTree(Path root) {
    this.root = root;
  }

  @Override
  public MediaItem getRoot() {
    return new MediaItem(this, new LocalInfo("SERIE_ROOT", "Series")) {
      @Override
      public List<? extends MediaItem> children() {
        if(children == null) {
          children = new SerieScanner(SeriesMediaTree.this).scan(root);
        }

        return children;
      }
    };
  }
}
