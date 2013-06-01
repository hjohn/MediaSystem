package hs.mediasystem.screens;

public interface LocationHandler {
  Presentation go(Location location, Presentation current);
  Class<? extends Location> getLocationType();
}
