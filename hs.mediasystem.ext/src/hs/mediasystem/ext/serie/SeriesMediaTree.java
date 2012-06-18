package hs.mediasystem.ext.serie;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.SerieScanner;
import hs.mediasystem.persist.PersistQueue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SeriesMediaTree implements MediaTree, MediaRoot {
  private final EnrichCache enrichCache;
  private final PersistQueue persister;
  private final Path root;

  private List<MediaItem> children;

  public SeriesMediaTree(EnrichCache enrichCache, PersistQueue persister, Path root) {
    this.enrichCache = enrichCache;
    this.persister = persister;
    this.root = root;
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new SerieScanner().scan(root);

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        SerieBase serie = new SerieBase(localInfo.getTitle());
        children.add(new SerieItem(SeriesMediaTree.this, localInfo.getUri(), serie));
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "Series";
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
    return "serieRoot";
  }
}
