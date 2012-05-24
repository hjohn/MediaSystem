package hs.mediasystem.screens;

import java.util.Comparator;

public class MediaNodeComparator implements Comparator<MediaNode> {
  public static final Comparator<MediaNode> INSTANCE = new MediaNodeComparator();

  @Override
  public int compare(MediaNode o1, MediaNode o2) {
    int result = o1.getMedia().getTitle().compareTo(o2.getMedia().getTitle());

    if(result == 0) {
      //result = Integer.compare(o1.getSeason() != null ? o1.getSeason() : Integer.MAX_VALUE, o2.getSeason() != null ? o2.getSeason() : Integer.MAX_VALUE);

      if(result == 0) {
        // result = o1.getSubtitle().compareTo(o2.getSubtitle());
      }
    }

    return result;
  }
}
