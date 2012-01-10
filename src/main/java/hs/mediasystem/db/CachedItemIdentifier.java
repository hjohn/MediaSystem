package hs.mediasystem.db;

import javax.inject.Inject;

public class CachedItemIdentifier implements ItemIdentifier {
  private final ItemsDao itemsDao;
  private final ItemIdentifier cachedItemIdentifier;

  @Inject
  public CachedItemIdentifier(ItemsDao itemsDao, @Cachable ItemIdentifier cachedItemIdentifier) {
    this.itemsDao = itemsDao;
    this.cachedItemIdentifier = cachedItemIdentifier;
  }

  @Override
  public String identifyItem(Item item) {
    try {
      Item dbItem = itemsDao.getItem(item);

      return dbItem.getProviderId();
    }
    catch (ItemNotFoundException e) {
      return cachedItemIdentifier.identifyItem(item);
    }
  }
}
