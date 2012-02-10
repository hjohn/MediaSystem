package hs.mediasystem.framework;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;

/**
 * A representation of a group of (related) media with navigation and display information.
 */
public interface MediaTree {
  List<? extends MediaItem> children();
  MediaTree parent();
  void queue(MediaItem mediaItem);
  ObjectProperty<EventHandler<MediaItemEvent>> onItemQueued();
}
