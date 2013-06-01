package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public class MovieTitleGroupingComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new MovieTitleGroupingComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    int result = o1.getTitle().compareTo(o2.getTitle());

    if(result == 0) {
      Integer s1 = Integer.parseInt(o1.sequence.get() == null ? "0" : o1.sequence.get());
      Integer s2 = Integer.parseInt(o2.sequence.get() == null ? "0" : o2.sequence.get());

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
