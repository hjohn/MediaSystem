package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public class EpisodeComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new EpisodeComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    Episode ep1 = o1.get(Episode.class);
    Episode ep2 = o2.get(Episode.class);

    int s1 = ep1 == null || ep1.season.get() == null || ep1.season.get() == 0 ? Integer.MAX_VALUE : ep1.season.get();
    int s2 = ep2 == null || ep2.season.get() == null || ep2.season.get() == 0 ? Integer.MAX_VALUE : ep2.season.get();

    int result = Integer.compare(s1, s2);

    if(result == 0) {
      int e1 = ep1 == null || ep1.episode.get() == null ? Integer.MAX_VALUE : ep1.episode.get();
      int e2 = ep2 == null || ep2.episode.get() == null ? Integer.MAX_VALUE : ep2.episode.get();

      result = Integer.compare(e1, e2);

      if(result == 0) {
        result = o1.getTitle().compareTo(o2.getTitle());
      }
    }

    return result;
  }

}
