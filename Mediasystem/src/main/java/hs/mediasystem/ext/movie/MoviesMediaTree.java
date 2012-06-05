package hs.mediasystem.ext.movie;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.EpisodeScanner;
import hs.mediasystem.persist.Persister;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MoviesMediaTree implements MediaTree, MediaRoot {
  private final EnrichCache enrichCache;
  private final Persister persister;
  private final Path root;

  private List<MediaItem> children;

  public MoviesMediaTree(EnrichCache enrichCache, Persister persister, Path root) {
    this.enrichCache = enrichCache;
    this.persister = persister;
    this.root = root;
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new EpisodeScanner(new MovieDecoder()).scan(root);

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        MovieBase movie = new MovieBase(localInfo.getTitle(), localInfo.getEpisode(), localInfo.getSubtitle(), localInfo.getReleaseYear(), localInfo.getCode());

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
  public EnrichCache getEnrichCache() {
    return enrichCache;
  }

  @Override
  public Persister getPersister() {
    return persister;
  }
}
