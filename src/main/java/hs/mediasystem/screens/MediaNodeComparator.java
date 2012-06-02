package hs.mediasystem.screens;

import java.util.Comparator;

public class MediaNodeComparator implements Comparator<MediaNode> {
  public static final Comparator<MediaNode> INSTANCE = new MediaNodeComparator();

  @Override
  public int compare(MediaNode o1, MediaNode o2) {
    return o1.getMedia().getTitle().compareTo(o2.getMedia().getTitle());
  }
}
