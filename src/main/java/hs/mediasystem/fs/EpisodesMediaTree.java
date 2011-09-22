package hs.mediasystem.fs;

import hs.mediasystem.db.CachedItemProvider;
import hs.mediasystem.db.TvdbEpisodeProvider;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.Renderer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EpisodesMediaTree extends AbstractMediaTree {
  private final Path root;
  
  private List<? extends MediaItem> children;

  public EpisodesMediaTree(Path root, Serie serie) {
    super(new CachedItemProvider(new TvdbEpisodeProvider(serie.getProviderId())));
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
      List<NamedItem> items = new ArrayList<NamedItem>();
      
      Collection<List<NamedItem>> groupedItems = Groups.group(episodes, new SeasonGrouper());
      
      for(List<NamedItem> group : groupedItems) {
        if(group.size() > 1) {
          Season s = new Season(this, group.get(0) instanceof Episode ? "" + ((Episode)group.get(0)).getSeason() : "Unknown");
          
          for(NamedItem item : group) {
            s.add(item);
          }
          
          items.add(s);
        }
        else {
          items.add(group.get(0));
        }
      }
      
      Collections.sort(items, new Comparator<NamedItem>() {
        @Override
        public int compare(NamedItem o1, NamedItem o2) {
          return o1.getTitle().compareTo(o2.getTitle());
        }
      });
      
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
}
