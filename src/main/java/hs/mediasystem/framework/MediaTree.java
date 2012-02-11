package hs.mediasystem.framework;

import javafx.beans.property.ObjectProperty;
import javafx.event.EventHandler;

/**
 * A representation of a group of (related) media with navigation and display information.
 */
public interface MediaTree {
  MediaItem getRoot();
  void queue(MediaItem mediaItem);
  ObjectProperty<EventHandler<MediaItemEvent>> onItemQueued();
}
