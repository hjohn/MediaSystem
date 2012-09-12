package hs.mediasystem.framework;


import java.util.Comparator;
import java.util.Date;

public class ChronologicalMediaItemComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new ChronologicalMediaItemComparator();

  private static final Date MIN_DATE = new Date(Long.MIN_VALUE);

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    Media<?> m1 = o1.get(Media.class);
    Media<?> m2 = o2.get(Media.class);
    Date d1 = m1.releaseDate.get() == null ? MIN_DATE : m1.releaseDate.get();
    Date d2 = m2.releaseDate.get() == null ? MIN_DATE : m2.releaseDate.get();

    int result = d1.compareTo(d2);

    if(result == 0) {
      result = m1.title.get().compareTo(m2.title.get());
    }

    return result;
  }

}
