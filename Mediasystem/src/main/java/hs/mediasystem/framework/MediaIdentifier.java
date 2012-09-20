package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;

public interface MediaIdentifier {
  Identifier identifyItem(MediaItem mediaItem) throws IdentifyException;
}
