package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.MediaItem;

import java.nio.file.Path;
import java.util.List;

public class MoviesMediaTree extends AbstractMediaTree {
  private final Path root;

  private List<? extends MediaItem> children;

  public MoviesMediaTree(Path root) {
    this.root = root;
  }

  @Override
  public MediaItem getRoot() {
    return new MediaItem(this, new LocalInfo(MediaType.MOVIE_ROOT, "Movies")) {
      @Override
      public List<? extends MediaItem> children() {
        if(children == null) {
          children = new EpisodeScanner(MoviesMediaTree.this, new MovieDecoder(), MediaType.MOVIE).scan(root);
        }

        return children;
      }
    };
  }
}
