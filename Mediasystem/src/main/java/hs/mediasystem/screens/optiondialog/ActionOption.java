package hs.mediasystem.screens.optiondialog;

import javafx.event.Event;
import javafx.event.EventHandler;

public class ActionOption extends Option {
  private final EventHandler<? super Event> eventHandler;

  public ActionOption(String description, EventHandler<? super Event> eventHandler) {
    super(description);
    this.eventHandler = eventHandler;
  }

  @Override
  public boolean select(Event event) {
    eventHandler.handle(event);
    return false;
  }
}
