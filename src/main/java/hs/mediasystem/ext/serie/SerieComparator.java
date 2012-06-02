package hs.mediasystem.ext.serie;

import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public class SerieComparator implements Comparator<MediaItem> {
  public static final Comparator<MediaItem> INSTANCE = new SerieComparator();

  @Override
  public int compare(MediaItem o1, MediaItem o2) {
    return o1.getTitle().compareTo(o2.getTitle());
  }
}
