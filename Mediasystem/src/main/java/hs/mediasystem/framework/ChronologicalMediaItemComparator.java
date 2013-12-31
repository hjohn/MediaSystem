package hs.mediasystem.framework;

import java.time.LocalDate;
import java.util.Comparator;

public class ChronologicalMediaItemComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new ChronologicalMediaItemComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    Media m1 = o1.getMedia();
    Media m2 = o2.getMedia();

    LocalDate d1 = m1.releaseDate.get() == null ? LocalDate.MIN : m1.releaseDate.get();
    LocalDate d2 = m2.releaseDate.get() == null ? LocalDate.MIN : m2.releaseDate.get();

    int result = d1.compareTo(d2);

    if(result == 0) {
      result = m1.title.get().compareTo(m2.title.get());
    }

    return result;
  }

}
