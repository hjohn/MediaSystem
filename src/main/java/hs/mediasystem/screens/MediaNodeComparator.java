package hs.mediasystem.screens;

import hs.mediasystem.fs.MediaItemComparator;

import java.util.Comparator;

public class MediaNodeComparator implements Comparator<MediaNode> {
  public static final Comparator<MediaNode> INSTANCE = new MediaNodeComparator();

  @Override
  public int compare(MediaNode o1, MediaNode o2) {
    return MediaItemComparator.INSTANCE.compare(o1.getMediaItem(), o2.getMediaItem());
  }
}
