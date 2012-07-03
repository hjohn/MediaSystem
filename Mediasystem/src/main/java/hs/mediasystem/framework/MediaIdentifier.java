package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;

public interface MediaIdentifier {
  Identifier identifyItem(Media media) throws IdentifyException;
}
