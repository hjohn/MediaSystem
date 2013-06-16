package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.Casting;
import javafx.event.Event;
import javafx.event.EventType;

public class CastingSelectedEvent extends Event {
  private final Casting casting;

  public CastingSelectedEvent(Casting casting) {
    super(EventType.ROOT);
    this.casting = casting;
  }

  public Casting getCasting() {
    return casting;
  }
}