package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo.Type;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MoviesMediaTree extends AbstractMediaTree {
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
      List<Episode> episodes = new EpisodeScanner(this, new MovieDecoder(), Type.MOVIE).scan(root);
      List<MediaItem> items = new ArrayList<>();

      Collection<List<MediaItem>> groupedItems = Groups.group(episodes, new TitleGrouper());

      for(List<MediaItem> group : groupedItems) {
        if(group.size() > 1) {
          EpisodeGroup g = new EpisodeGroup(this, group.get(0).getTitle());

          for(MediaItem item : group) {
            g.add(item);
          }

          items.add(g);
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
    return null;
  }

  @Override
  public CellProvider<MediaItem> createListCell() {
    return new DuoLineRenderer();
  }
}
