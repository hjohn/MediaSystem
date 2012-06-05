package hs.mediasystem.framework;

import hs.mediasystem.enrich.EnrichTrigger;
import javafx.event.Event;
import javafx.event.EventType;

public class MediaItemEvent extends Event {
  private final EnrichTrigger mediaItem;

  public MediaItemEvent(EnrichTrigger mediaItem) {
    super(EventType.ROOT);
    this.mediaItem = mediaItem;
  }

  public EnrichTrigger getMediaItem() {
    return mediaItem;
  }
}