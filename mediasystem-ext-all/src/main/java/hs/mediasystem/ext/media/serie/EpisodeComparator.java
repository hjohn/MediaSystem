package hs.mediasystem.ext.media.serie;

import java.util.Comparator;

public class EpisodeComparator implements Comparator<Episode> {
  public static final Comparator<Episode> INSTANCE = new EpisodeComparator();

  @Override
  public int compare(Episode o1, Episode o2) {
    Integer s1 = o1.season.get();
    Integer s2 = o2.season.get();

    s1 = s1 == null ? Integer.MAX_VALUE : s1;
    s2 = s2 == null ? Integer.MAX_VALUE : s2;

    int result = Integer.compare(s1, s2);

    if(result == 0) {
      Integer e1 = o1.episode.get();
      Integer e2 = o2.episode.get();

      e1 = e1 == null ? Integer.MAX_VALUE : e1;
      e2 = e2 == null ? Integer.MAX_VALUE : e2;

      result = Integer.compare(e1, e2);
    }

    return result;
  }
}
