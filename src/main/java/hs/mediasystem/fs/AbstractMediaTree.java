package hs.mediasystem.fs;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemEvent;
import hs.mediasystem.framework.MediaTree;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;

public abstract class AbstractMediaTree implements MediaTree {

  @Override
  public void queue(MediaItem mediaItem) {
    if(onItemQueued.get() != null) {
      onItemQueued.get().handle(new MediaItemEvent(mediaItem));
    }
  }

  private final ObjectProperty<EventHandler<MediaItemEvent>> onItemQueued = new SimpleObjectProperty<>();
  @Override public ObjectProperty<EventHandler<MediaItemEvent>> onItemQueued() { return onItemQueued; }
}
