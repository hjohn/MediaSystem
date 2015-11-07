package hs.mediasystem.ext.media.movie;

import java.util.Comparator;

public class MovieTitleGroupingComparator implements Comparator<Movie> {
  public static final Comparator<Movie> INSTANCE = new MovieTitleGroupingComparator();

  @Override
  public int compare(Movie o1, Movie o2) {
    int result = o1.initialTitle.get().compareTo(o2.initialTitle.get());

    if(result == 0) {
      Integer s1 = o1.sequence.get() == null ? 0 : o1.sequence.get();
      Integer s2 = o2.sequence.get() == null ? 0 : o2.sequence.get();

      result = Integer.compare(s1, s2);

      if(result == 0) {
        String sub1 = o1.subtitle.get() == null ? "" : o1.subtitle.get();
        String sub2 = o2.subtitle.get() == null ? "" : o2.subtitle.get();

        result = sub1.compareTo(sub2);
      }
    }

    return result;
  }

}
