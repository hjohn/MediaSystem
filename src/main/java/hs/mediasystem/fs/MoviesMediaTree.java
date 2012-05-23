package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.media.Movie;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MoviesMediaTree extends AbstractMediaTree implements MediaRoot {
  private final Path root;

  private List<MediaItem> children;

  public MoviesMediaTree(Path root) {
    this.root = root;
  }

  @Override
  public MediaItem getRoot() {
    return new MediaItem("MOVIE_ROOT", this) {
      @Override
      public List<? extends MediaItem> children() {
        return getItems();
      }
    };
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new EpisodeScanner(new MovieDecoder()).scan(root);

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        Movie movie = new Movie(localInfo.getTitle(), localInfo.getEpisode(), localInfo.getSubtitle(), localInfo.getReleaseYear(), localInfo.getCode());

        children.add(new MediaItem(MoviesMediaTree.this, localInfo.getUri(), true, movie));
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "Movies";
  }
}
