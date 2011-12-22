package hs.mediasystem.framework;

import hs.mediasystem.fs.CellProvider;
import hs.mediasystem.fs.NamedItem;

import java.util.List;

/**
 * A representation of a group of (related) media with navigation and display information.
 */
public interface MediaTree {
  public enum Style {BANNER, LIST}
  
  void refresh();

  Style getStyle();

  List<? extends MediaItem> children();
  MediaTree parent();
  
  CellProvider<MediaItem> createListCell();

  void enrichItem(final NamedItem namedItem);
}
