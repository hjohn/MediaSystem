package hs.mediasystem.fs;

import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaTree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EpisodesMediaTree extends AbstractMediaTree {
  private final Path root;
  private final String serieName;

  private List<? extends MediaItem> children;

  public EpisodesMediaTree(Path root, String serieName) {
    this.root = root;
    this.serieName = serieName;
  }

  @Override
  public List<? extends MediaItem> children() {
    if(children == null) {
      List<Episode> episodes = new EpisodeScanner(this, new EpisodeDecoder(), MediaType.EPISODE).scan(root);
      List<MediaItem> items = new ArrayList<>();

      Collection<List<MediaItem>> groupedItems = Groups.group(episodes, new SeasonGrouper());

      for(List<MediaItem> group : groupedItems) {
        if(group.size() > 1) {
          Episode episodeOne = (Episode)group.get(0);
          Season s = new Season(this, serieName, episodeOne.getSeason());

          Collections.sort(group, EpisodeComparator.INSTANCE);

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
}
