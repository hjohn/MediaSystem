package hs.mediasystem.db;

import hs.mediasystem.framework.Media;

public interface ItemEnricher {
  String getProviderCode();
  EnricherMatch identifyItem(Media media) throws IdentifyException;
  Item loadItem(String identifier) throws ItemNotFoundException;
}
