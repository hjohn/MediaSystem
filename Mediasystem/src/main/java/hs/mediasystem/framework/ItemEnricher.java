package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;

public interface ItemEnricher {
  String getProviderCode();
  Identifier identifyItem(Media media) throws IdentifyException;
  Item loadItem(String identifier) throws ItemNotFoundException;
}
