package hs.mediasystem.framework;

import hs.mediasystem.fs.NamedItem;
import hs.mediasystem.screens.movie.ItemUpdate;
import hs.models.events.ListenerList;

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
  
  Renderer<MediaItem> getRenderer();

  ListenerList<ItemUpdate> onItemUpdate();
  
  void triggerItemUpdate(NamedItem namedItem);
}
