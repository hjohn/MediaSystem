package hs.mediasystem.util;

import javafx.event.Event;
import javafx.event.EventType;

public class DialogEvent extends Event {
  public static final EventType<DialogEvent> ANY = new EventType<>(EventType.ROOT, "DIALOG");
  public static final EventType<DialogEvent> DIALOG_SHOWN = new EventType<>(ANY, "DIALOG_SHOWN");
  public static final EventType<DialogEvent> DIALOG_CLOSED = new EventType<>(ANY, "DIALOG_CLOSED");

  public DialogEvent(boolean shown) {
    super(shown ? DIALOG_SHOWN : DIALOG_CLOSED);
  }
}
