package hs.mediasystem.fs;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.media.Episode;

public class SeasonGrouper implements Grouper<MediaItem> {

  @Override
  public Object getGroup(MediaItem item) {
    Episode episode = item.get(Episode.class);

    return episode.getSeason();
  }
}
