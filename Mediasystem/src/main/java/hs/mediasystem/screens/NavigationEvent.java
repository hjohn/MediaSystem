package hs.mediasystem.screens;

import javafx.event.Event;
import javafx.event.EventType;

public class NavigationEvent extends Event {
  public static final EventType<NavigationEvent> ANY = new EventType<>(EventType.ROOT, "NAVIGATION");

  public static final EventType<NavigationEvent> NAVIGATION_LEFT = new EventType<>(ANY, "NAVIGATION_LEFT");
  public static final EventType<NavigationEvent> NAVIGATION_RIGHT = new EventType<>(ANY, "NAVIGATION_RIGHT");
  public static final EventType<NavigationEvent> NAVIGATION_UP = new EventType<>(ANY, "NAVIGATION_UP");
  public static final EventType<NavigationEvent> NAVIGATION_DOWN = new EventType<>(ANY, "NAVIGATION_DOWN");
  public static final EventType<NavigationEvent> NAVIGATION_SELECT = new EventType<>(ANY, "NAVIGATION_SELECT");
  public static final EventType<NavigationEvent> NAVIGATION_ANCESTOR = new EventType<>(ANY, "NAVIGATION_ANCESTOR");

  public static final EventType<NavigationEvent> NAVIGATION_EXIT = new EventType<>(NAVIGATION_ANCESTOR, "NAVIGATION_EXIT");
  public static final EventType<NavigationEvent> NAVIGATION_BACK = new EventType<>(NAVIGATION_ANCESTOR, "NAVIGATION_BACK");

  public NavigationEvent(EventType<NavigationEvent> navigationEvent) {
    super(navigationEvent);
  }
}
