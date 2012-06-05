package hs.mediasystem.screens;

import hs.mediasystem.fs.ChronologicalMediaItemComparator;

import java.util.Comparator;

public class ChronologicalMediaNodeComparator implements Comparator<MediaNode> {
  public static final Comparator<MediaNode> INSTANCE = new ChronologicalMediaNodeComparator();

  @Override
  public int compare(MediaNode o1, MediaNode o2) {
    return ChronologicalMediaItemComparator.INSTANCE.compare(o1.getMediaItem(), o2.getMediaItem());
  }
}
