package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.MediaItem;

public class SeasonGrouper implements Grouper<MediaItem> {

  @Override
  public Object getGroup(MediaItem item) {
    Episode episode = item.get(Episode.class);

    return episode.season.get();
  }
}
