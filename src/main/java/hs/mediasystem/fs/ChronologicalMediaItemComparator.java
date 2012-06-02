package hs.mediasystem.fs;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.media.Media;

import java.util.Comparator;
import java.util.Date;

public class ChronologicalMediaItemComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new ChronologicalMediaItemComparator();

  private static final Date MIN_DATE = new Date(Long.MIN_VALUE);

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    Media m1 = o1.get(Media.class);
    Media m2 = o2.get(Media.class);
    Date d1 = m1.getReleaseDate() == null ? MIN_DATE : m1.getReleaseDate();
    Date d2 = m2.getReleaseDate() == null ? MIN_DATE : m2.getReleaseDate();

    int result = d1.compareTo(d2);

    if(result == 0) {
      result = m1.getTitle().compareTo(m2.getTitle());
    }

    return result;
  }

}
