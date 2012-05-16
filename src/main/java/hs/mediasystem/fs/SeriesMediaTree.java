package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SeriesMediaTree extends AbstractMediaTree {
  private final Path root;

  private List<MediaItem> children;

  public SeriesMediaTree(Path root) {
    this.root = root;
  }

  @Override
  public MediaItem getRoot() {
    return new MediaItem(this, new LocalInfo<>(root.toString(), "SERIE_ROOT", "Series"), false) {
      @Override
      public List<? extends MediaItem> children() {
        if(children == null) {
          List<LocalInfo<Object>> scanResults = new SerieScanner().scan(root);

          children = new ArrayList<>();

          for(LocalInfo<Object> localInfo : scanResults) {
            children.add(new Serie(SeriesMediaTree.this, localInfo));
          }
        }

        return children;
      }
    };
  }
}
