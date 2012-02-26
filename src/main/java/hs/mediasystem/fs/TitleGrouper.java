package hs.mediasystem.fs;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.MediaItem;

public class TitleGrouper implements Grouper<MediaItem> {

  @Override
  public Object getGroup(MediaItem item) {
    if(item.getEpisode() != null) {
      return item.getTitle();
    }
    return item.getTitle() + "/" + item.getReleaseYear();
  }

}
