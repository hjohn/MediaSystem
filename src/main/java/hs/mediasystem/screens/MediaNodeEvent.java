package hs.mediasystem.screens;

import javafx.event.Event;
import javafx.event.EventType;

public class MediaNodeEvent extends Event {
  private final MediaNode mediaNode;

  public MediaNodeEvent(MediaNode mediaNode) {
    super(EventType.ROOT);
    this.mediaNode = mediaNode;
  }

  public MediaNode getMediaNode() {
    return mediaNode;
  }
}