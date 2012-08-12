package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.persist.PersistQueue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SeriesMediaTree implements MediaTree, MediaRoot {
  private final EnrichCache enrichCache;
  private final PersistQueue persister;
  private final List<Path> roots;

  private List<MediaItem> children;

  public SeriesMediaTree(EnrichCache enrichCache, PersistQueue persister, List<Path> roots) {
    this.enrichCache = enrichCache;
    this.persister = persister;
    this.roots = new ArrayList<>(roots);
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Path root : roots) {
        List<LocalInfo> scanResults = new SerieScanner().scan(root);

        for(LocalInfo localInfo : scanResults) {
          SerieBase serie = new SerieBase(localInfo.getTitle());
          children.add(new SerieItem(SeriesMediaTree.this, localInfo.getUri(), serie));
        }
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
