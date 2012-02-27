package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MoviesMediaTree extends AbstractMediaTree {
  private final Path root;

  private List<MediaItem> children;

  public MoviesMediaTree(Path root) {
    this.root = root;
  }

  @Override
  public MediaItem getRoot() {
    return new MediaItem(this, new LocalInfo<>("MOVIE_ROOT", "Movies")) {
      @Override
      public List<? extends MediaItem> children() {
        if(children == null) {
          List<LocalInfo<Object>> scanResults = new EpisodeScanner(new MovieDecoder(), "MOVIE").scan(root);

          children = new ArrayList<>();

          for(LocalInfo<Object> localInfo : scanResults) {
            children.add(new MediaItem(MoviesMediaTree.this, localInfo));
          }
        }

        return children;
      }
    };
  }
}
