package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.media.Movie;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MoviesMediaTree implements MediaTree, MediaRoot {
  private final EnrichCache<MediaItem> enrichCache;
  private final Path root;

  private List<MediaItem> children;

  public MoviesMediaTree(EnrichCache<MediaItem> enrichCache, Path root) {
    this.enrichCache = enrichCache;
    this.root = root;
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new EpisodeScanner(new MovieDecoder()).scan(root);

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        Movie movie = new Movie(localInfo.getTitle(), localInfo.getEpisode(), localInfo.getSubtitle(), localInfo.getReleaseYear(), localInfo.getCode());

        children.add(new MediaItem(MoviesMediaTree.this, localInfo.getUri(), movie));
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "Movies";
  }

  @Override
  public EnrichCache<MediaItem> getEnrichCache() {
    return enrichCache;
  }
}
