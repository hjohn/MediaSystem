package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SeriesMediaTree implements MediaTree, MediaRoot {
  private final EnrichCache<MediaItem> enrichCache;
  private final Path root;

  private List<MediaItem> children;

  public SeriesMediaTree(EnrichCache<MediaItem> enrichCache, Path root) {
    this.enrichCache = enrichCache;
    this.root = root;
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new SerieScanner().scan(root);

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        hs.mediasystem.media.Serie serie = new hs.mediasystem.media.Serie(localInfo.getTitle());
        children.add(new Serie(SeriesMediaTree.this, localInfo.getUri(), serie));
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "Series";
  }

  @Override
  public EnrichCache<MediaItem> getEnrichCache() {
    return enrichCache;
  }
}
