package hs.mediasystem.fs;

import hs.mediasystem.db.CachedItemEnricher;
import hs.mediasystem.db.TvdbEpisodeEnricher;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Renderer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.scene.control.ListCell;

public class EpisodesMediaTree extends AbstractMediaTree {
  private final Path root;
  
  private List<? extends MediaItem> children;

  public EpisodesMediaTree(Path root, Serie serie) {
    super(new CachedItemEnricher(new TvdbEpisodeEnricher(serie.getProviderId())));
    this.root = root;
  }

  @Override
  public void refresh() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public Style getStyle() {
    return Style.LIST;
  }

  @Override
  public List<? extends MediaItem> children() {
    if(children == null) {
      List<Episode> episodes = new EpisodeScanner(this, new EpisodeDecoder()).scan(root);
      List<MediaItem> items = new ArrayList<MediaItem>();
      
      Collection<List<MediaItem>> groupedItems = Groups.group(episodes, new SeasonGrouper());
      
      for(List<MediaItem> group : groupedItems) {
        if(group.size() > 1) {
          Season s = new Season(this, group.get(0) instanceof Episode ? "" + ((Episode)group.get(0)).getSeason() : "Unknown");
          
          for(MediaItem item : group) {
            s.add(item);
          }
          
          items.add(s);
        }
        else {
          items.add(group.get(0));
        }
      }
      
      Collections.sort(items, MediaItemComparator.INSTANCE);
      
      children = items;
    }
    
    return children;
  } 

  @Override
  public MediaTree parent() {
    throw new UnsupportedOperationException("Method not implemented");
  }

  @Override
  public Renderer<MediaItem> getRenderer() {
    return new DuoLineRenderer();
  }

  @Override
  public ListCell<MediaItem> createListCell() {
    throw new UnsupportedOperationException("Method not implemented");
  }
}
