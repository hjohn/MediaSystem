package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public class EpisodeComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new EpisodeComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    Integer s1 = (Integer)o1.properties.get("season");
    Integer s2 = (Integer)o2.properties.get("season");

    s1 = s1 == null ? Integer.MAX_VALUE : s1;
    s2 = s2 == null ? Integer.MAX_VALUE : s2;

    int result = Integer.compare(s1, s2);

    if(result == 0) {
      Integer e1 = (Integer)o1.properties.get("episodeNumber");
      Integer e2 = (Integer)o2.properties.get("episodeNumber");

      e1 = e1 == null ? Integer.MAX_VALUE : e1;
      e2 = e2 == null ? Integer.MAX_VALUE : e2;

      result = Integer.compare(e1, e2);
    }

    return result;
  }

}
