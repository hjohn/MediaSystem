package hs.mediasystem.util;

import hs.mediasystem.screens.Presentation;

public interface LocationHandler {
  Presentation go(Location location, Presentation current);
}
