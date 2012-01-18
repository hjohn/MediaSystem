package hs.mediasystem.framework;

import java.util.List;

/**
 * A representation of a group of (related) media with navigation and display information.
 */
public interface MediaTree {
  List<? extends MediaItem> children();
  MediaTree parent();
  CellProvider<MediaItem> createListCell();
}
