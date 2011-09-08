package hs.mediasystem;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MoviesMediaTree implements MediaTree {
  private final Path root;

  private List<? extends MediaItem> children;
  
  public MoviesMediaTree(Path root) {
    this.root = root;
  }
  
  @Override
  public void refresh() {
    children = null;
  }

  @Override
  public Style getStyle() {
    return Style.LIST;
  }

  @Override
  public List<? extends MediaItem> children() {
    if(children == null) {
      List<Episode> episodes = new MovieScanner(new MovieElementDecoder()).scan(root);
      List<NamedItem> items = new ArrayList<NamedItem>();
      
      Collection<List<NamedItem>> groupedItems = Groups.group(episodes, new TitleGrouper());
      
      for(List<NamedItem> group : groupedItems) {
        if(group.size() > 1) {
          EpisodeGroup g = new EpisodeGroup(group.get(0).getTitle());
          
          for(NamedItem item : group) {
            g.add(item);
          }
          
          items.add(g);
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
    return null;
  }
  
  @Override
  public Renderer<MediaItem> getRenderer() {
    return new DuoLineRenderer();
  }
}
