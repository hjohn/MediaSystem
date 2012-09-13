package hs.mediasystem.ext.media.movie;

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

      Integer s1 = m1 == null || m1.sequence.get() == null ? 0 : m1.sequence.get();
      Integer s2 = m2 == null || m2.sequence.get() == null ? 0 : m2.sequence.get();

      result = Integer.compare(s1, s2);

      if(result == 0) {
        Media<?> media1 = o1.getMedia();
        Media<?> media2 = o2.getMedia();

        result = media1.subtitle.get().compareTo(media2.subtitle.get());
      }
    }

    return result;
  }

}
