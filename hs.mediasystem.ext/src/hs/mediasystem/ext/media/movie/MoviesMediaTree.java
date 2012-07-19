package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.EpisodeScanner;
import hs.mediasystem.persist.PersistQueue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MoviesMediaTree implements MediaTree, MediaRoot {
  private final EnrichCache enrichCache;
  private final PersistQueue persister;
  private final List<Path> roots;

  private List<MediaItem> children;

  public MoviesMediaTree(EnrichCache enrichCache, PersistQueue persister, List<Path> roots) {
    this.enrichCache = enrichCache;
    this.persister = persister;
    this.roots = new ArrayList<>(roots);
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Path root : roots) {
        List<LocalInfo> scanResults = new EpisodeScanner(new MovieDecoder()).scan(root);

        for(LocalInfo localInfo : scanResults) {
          MovieBase movie = new MovieBase(localInfo.getTitle(), localInfo.getEpisode(), localInfo.getSubtitle(), localInfo.getReleaseYear(), localInfo.getCode());

          children.add(new MediaItem(MoviesMediaTree.this, localInfo.getUri(), movie));
        }
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
  public PersistQueue getPersister() {
    return persister;
  }

  @Override
  public String getId() {
    return "movieRoot";
  }
}
