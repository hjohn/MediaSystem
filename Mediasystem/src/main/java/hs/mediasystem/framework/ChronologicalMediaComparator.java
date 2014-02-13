package hs.mediasystem.framework;

import java.time.LocalDate;
import java.util.Comparator;

public class ChronologicalMediaComparator implements Comparator<Media> {
  public static final Comparator<Media> INSTANCE = new ChronologicalMediaComparator();

  @Override
  public int compare(Media o1, Media o2) {
    LocalDate d1 = o1.releaseDate.get() == null ? LocalDate.MIN : o1.releaseDate.get();
    LocalDate d2 = o2.releaseDate.get() == null ? LocalDate.MIN : o2.releaseDate.get();

    int result = d1.compareTo(d2);

    if(result == 0) {
      result = o1.title.get().compareTo(o2.title.get());
    }

    return result;
  }

}
