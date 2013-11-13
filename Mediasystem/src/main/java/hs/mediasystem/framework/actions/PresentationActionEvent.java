package hs.mediasystem.framework.actions;

import javafx.event.Event;
import javafx.event.EventType;

public class PresentationActionEvent<P> extends Event {
  public static final EventType<PresentationActionEvent<?>> PRESENTATION_ACTION = new EventType<>(Event.ANY, "PRESENTATION_ACTION");
  public static final EventType<PresentationActionEvent<?>> ANY = PRESENTATION_ACTION;

  private final P presentation;

  public PresentationActionEvent(P presentation, Event triggerEvent) {
    super(triggerEvent.getSource(), triggerEvent.getTarget(), PRESENTATION_ACTION);

    this.presentation = presentation;
  }

  public P getPresentation() {
    return presentation;
  }
}
