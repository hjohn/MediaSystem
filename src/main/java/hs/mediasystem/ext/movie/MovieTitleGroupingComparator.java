package hs.mediasystem.ext.movie;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public class MovieTitleGroupingComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new MovieTitleGroupingComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    int result = o1.getTitle().compareTo(o2.getTitle());

    if(result == 0) {
      Movie m1 = o1.get(Movie.class);
      Movie m2 = o2.get(Movie.class);

      Integer s1 = m1 == null || m1.getSequence() == null ? 0 : m1.getSequence();
      Integer s2 = m2 == null || m2.getSequence() == null ? 0 : m2.getSequence();

      result = Integer.compare(s1, s2);

      if(result == 0) {
        Media media1 = o1.get(Media.class);
        Media media2 = o2.get(Media.class);

        result = media1.getSubtitle().compareTo(media2.getSubtitle());
      }
    }

    return result;
  }

}
