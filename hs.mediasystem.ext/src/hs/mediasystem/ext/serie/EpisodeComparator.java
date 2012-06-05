package hs.mediasystem.ext.serie;

import hs.mediasystem.framework.Episode;
import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public class EpisodeComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new EpisodeComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    Episode ep1 = o1.get(Episode.class);
    Episode ep2 = o2.get(Episode.class);

    int s1 = ep1 == null || ep1.getSeason() == null || ep1.getSeason() == 0 ? Integer.MAX_VALUE : ep1.getSeason();
    int s2 = ep2 == null || ep2.getSeason() == null || ep2.getSeason() == 0 ? Integer.MAX_VALUE : ep2.getSeason();

    int result = Integer.compare(s1, s2);

    if(result == 0) {
      int e1 = ep1 == null || ep1.getEpisode() == null ? Integer.MAX_VALUE : ep1.getEpisode();
      int e2 = ep2 == null || ep2.getEpisode() == null ? Integer.MAX_VALUE : ep2.getEpisode();

      result = Integer.compare(e1, e2);

      if(result == 0) {
        result = o1.getTitle().compareTo(o2.getTitle());
      }
    }

    return result;
  }

}
