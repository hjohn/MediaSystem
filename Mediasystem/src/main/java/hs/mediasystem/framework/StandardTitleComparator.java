package hs.mediasystem.framework;

import java.util.Comparator;

public class StandardTitleComparator implements Comparator<Media> {
  public static final Comparator<Media> INSTANCE = new StandardTitleComparator();

  @Override
  public int compare(Media o1, Media o2) {
    return o1.initialTitle.get().compareTo(o2.initialTitle.get());
  }
}
