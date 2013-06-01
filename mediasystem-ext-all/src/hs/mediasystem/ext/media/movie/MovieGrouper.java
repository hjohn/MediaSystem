package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.MediaItem;

public class MovieGrouper implements Grouper<MediaItem> {

  @Override
  public Object getGroup(MediaItem item) {
    if(item.sequence.get() != null) {
      return item.title.get();
    }
    return item.getUri();
  }
}
