package hs.mediasystem.framework;


import java.util.Comparator;

public class StandardTitleComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new StandardTitleComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    return o1.getTitle().compareTo(o2.getTitle());
  }
}
