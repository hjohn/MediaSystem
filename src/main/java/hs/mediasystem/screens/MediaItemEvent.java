package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import javafx.event.Event;
import javafx.event.EventType;

public class MediaItemEvent extends Event {
  private final MediaItem mediaItem;

  public MediaItemEvent(MediaItem mediaItem) {
    super(EventType.ROOT);
    this.mediaItem = mediaItem;
  }

  public MediaItem getMediaItem() {
    return mediaItem;
  }
}