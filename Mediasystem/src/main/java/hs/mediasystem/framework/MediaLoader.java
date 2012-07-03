package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;

public interface MediaLoader {
  Item loadItem(Identifier identifier) throws ItemNotFoundException;
}
