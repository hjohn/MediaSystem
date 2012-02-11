package hs.mediasystem.screens;

import hs.mediasystem.db.MediaType;
import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.fs.EpisodeComparator;
import hs.mediasystem.fs.EpisodeGroup;
import hs.mediasystem.fs.MediaItemComparator;
import hs.mediasystem.fs.Season;
import hs.mediasystem.fs.SeasonGrouper;
import hs.mediasystem.fs.TitleGrouper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class StandardLayout {

  public CellProvider<MediaItem> getCellProvider(MediaItem parent) {
    MediaType mediaType = parent.getMediaType();

    if(mediaType == MediaType.MOVIE_ROOT) {
      return new MovieCellProvider();
    }
    else if(mediaType == MediaType.SERIE_ROOT) {
      return new BannerCellProvider();
    }

    return new SeasonAndEpisodeCellProvider();
  }

  public List<? extends MediaItem> getChildren(MediaItem parent) {
    List<? extends MediaItem> children = parent.children();
    List<MediaItem> output = new ArrayList<>();

    if(parent.getMediaType() == MediaType.MOVIE_ROOT) {
      Collection<List<MediaItem>> groupedItems = Groups.group(children, new TitleGrouper());

      for(List<MediaItem> group : groupedItems) {
        if(group.size() > 1) {
          Collections.sort(group, MediaItemComparator.INSTANCE);
          EpisodeGroup g = new EpisodeGroup(parent.getMediaTree(), group);

          output.add(g);
        }
        else {
          output.add(group.get(0));
        }
      }
    }
    else if(parent.getMediaType() == MediaType.SERIE) {
      Collection<List<MediaItem>> groupedItems = Groups.group(children, new SeasonGrouper());

      for(List<MediaItem> group : groupedItems) {
        if(group.size() > 1) {
          MediaItem episodeOne = group.get(0);
          Season s = new Season(parent.getMediaTree(), parent.getTitle(), episodeOne.getSeason());

          Collections.sort(group, EpisodeComparator.INSTANCE);

          for(MediaItem item : group) {
            s.add(item);
          }

          output.add(s);
        }
        else {
          output.add(group.get(0));
        }
      }
    }
    else {
      output.addAll(children);
    }

    Collections.sort(output, MediaItemComparator.INSTANCE);

    return output;
  }

  public boolean isFilterLevel(MediaItem mediaItem) {
    return mediaItem.getMediaType() == MediaType.SERIE;
  }

  public boolean hasChildren(MediaItem mediaItem) {
    return !mediaItem.isLeaf();
  }

  public boolean isRoot(MediaItem mediaItem) {
    MediaType mediaType = mediaItem.getMediaType();

    return mediaType == MediaType.MOVIE_ROOT || mediaType == MediaType.SERIE_ROOT || mediaType == MediaType.SERIE;
  }
}
