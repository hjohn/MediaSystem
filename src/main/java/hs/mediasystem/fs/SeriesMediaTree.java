package hs.mediasystem.fs;

import hs.mediasystem.db.CachedItemEnricher;
import hs.mediasystem.db.TvdbSerieEnricher;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Renderer;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import javafx.scene.control.ListCell;

public class SeriesMediaTree extends AbstractMediaTree {
  private final Path root;

  private List<? extends MediaItem> children;
  
  public SeriesMediaTree(Path root) {
    super(new CachedItemEnricher(new TvdbSerieEnricher()));
    this.root = root;
  }
  
  @Override
  public void refresh() {
    children = null;
  }

  @Override
  public Style getStyle() {
    return Style.BANNER;
  }

  @Override
  public List<? extends MediaItem> children() {
    if(children == null) {
      children = new SerieScanner(this).scan(root);
      Collections.sort(children, MediaItemComparator.INSTANCE);
    }
    
    return children;
  }

  @Override
  public MediaTree parent() {
    return null;
  }

  @Override
  public Renderer<MediaItem> getRenderer() {
    return new MediaItemRenderer();
  }

  @Override
  public CellProvider<MediaItem> createListCell() {
    return new BannerRenderer();
  }
}
