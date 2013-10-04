package hs.mediasystem.screens.collection.detail;

import javafx.event.Event;
import javafx.event.EventType;

public class DetailNavigationEvent extends Event {
  private final Object content;

  public DetailNavigationEvent(Object content) {
    super(EventType.ROOT);

    this.content = content;
  }

  public Object getContent() {
    return content;
  }
}
