package hs.mediasystem.screens;

import hs.mediasystem.framework.ChronologicalMediaComparator;

import java.util.Comparator;

public class ChronologicalMediaNodeComparator implements Comparator<MediaNode> {
  public static final Comparator<MediaNode> INSTANCE = new ChronologicalMediaNodeComparator();

  @Override
  public int compare(MediaNode o1, MediaNode o2) {
    return ChronologicalMediaComparator.INSTANCE.compare(o1.getMedia(), o2.getMedia());
  }
}
