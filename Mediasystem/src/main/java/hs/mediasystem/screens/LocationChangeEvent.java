package hs.mediasystem.screens;

import javafx.event.Event;
import javafx.event.EventType;

public class LocationChangeEvent extends Event {
  public static final EventType<LocationChangeEvent> ANY = new EventType<>(EventType.ROOT, "LOCATION_CHANGE");
  public static final EventType<LocationChangeEvent> LOCATION_CHANGE = ANY;

  private final Location location;

  public LocationChangeEvent(Location location) {
    super(LOCATION_CHANGE);

    this.location = location;
  }

  public Location getLocation() {
    return location;
  }
}
