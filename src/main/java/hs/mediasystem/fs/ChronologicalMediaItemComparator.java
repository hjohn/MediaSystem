package hs.mediasystem.fs;

import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;
import java.util.Date;

public class ChronologicalMediaItemComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new ChronologicalMediaItemComparator();
  private static final Date MIN_DATE = new Date(Long.MIN_VALUE);

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    Date d1 = o1.getReleaseDate() == null ? MIN_DATE : o1.getReleaseDate();
    Date d2 = o2.getReleaseDate() == null ? MIN_DATE : o2.getReleaseDate();

    int result = d1.compareTo(d2);

    if(result == 0) {
      return MediaItemComparator.INSTANCE.compare(o1, o2);
    }

    return result;
  }

}
