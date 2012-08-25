package hs.mediasystem.screens;

import javafx.event.Event;
import javafx.event.EventType;

public class NavigationEvent extends Event {
  public static final EventType<NavigationEvent> ANY = new EventType<>(EventType.ROOT, "NAVIGATION");
  public static final EventType<NavigationEvent> NAVIGATION_BACK = new EventType<>(ANY, "NAVIGATION_BACK");
  public static final EventType<NavigationEvent> NAVIGATION_LEFT = new EventType<>(ANY, "NAVIGATION_LEFT");
  public static final EventType<NavigationEvent> NAVIGATION_RIGHT = new EventType<>(ANY, "NAVIGATION_RIGHT");
  public static final EventType<NavigationEvent> NAVIGATION_UP = new EventType<>(ANY, "NAVIGATION_UP");
  public static final EventType<NavigationEvent> NAVIGATION_DOWN = new EventType<>(ANY, "NAVIGATION_DOWN");
  public static final EventType<NavigationEvent> NAVIGATION_SELECT = new EventType<>(ANY, "NAVIGATION_SELECT");

  public NavigationEvent(EventType<NavigationEvent> navigationEvent) {
    super(navigationEvent);
  }
}
