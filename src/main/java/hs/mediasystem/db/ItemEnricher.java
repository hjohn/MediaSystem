package hs.mediasystem.db;

import hs.mediasystem.framework.MediaItem;

public interface ItemEnricher {
  String getProviderCode();
  EnricherMatch identifyItem(MediaItem mediaItem) throws IdentifyException;
  Item loadItem(String identifier, MediaItem mediaItem) throws ItemNotFoundException;
}
