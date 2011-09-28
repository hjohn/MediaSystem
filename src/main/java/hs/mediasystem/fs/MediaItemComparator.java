package hs.mediasystem.fs;

import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public class MediaItemComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new MediaItemComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    int result = o1.getTitle().compareTo(o2.getTitle());
    
    if(result == 0) {
      result = Integer.compare(o1.getSeason(), o2.getSeason()); 

      if(result == 0) {
        result = Integer.compare(o1.getEpisode(), o2.getEpisode());
        
        if(result == 0) {
          result = o1.getSubtitle().compareTo(o2.getSubtitle());
        }
      }
    }
    
    return result;
  }

}
