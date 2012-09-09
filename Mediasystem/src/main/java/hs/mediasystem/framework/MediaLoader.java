package hs.mediasystem.framework;

import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ProviderId;

public interface MediaLoader {
  Item loadItem(ProviderId providerId) throws ItemNotFoundException;
}
