package hs.mediasystem.db;

import javax.inject.Inject;

public class CachedItemEnricher {
  private final ItemsDao itemsDao;
  private final ItemEnricher providerToCache;

  @Inject
  public CachedItemEnricher(ItemsDao itemsDao, @Cachable ItemEnricher providerToCache) {
    this.itemsDao = itemsDao;
    this.providerToCache = providerToCache;
  }

  public Identifier identifyItem(LocalInfo localInfo, boolean bypassCache) throws IdentifyException {
    System.out.println("[FINE] CachedItemEnricher.identifyItem() - with surrogatename: " + localInfo.getSurrogateName());

    if(!bypassCache) {
      try {
        Identifier identifier = itemsDao.getQuery(localInfo.getSurrogateName());

        System.out.println("[FINE] CachedItemEnricher.identifyItem() - Cache Hit");

        return identifier;
      }
      catch(ItemNotFoundException e) {
        System.out.println("[FINE] CachedItemEnricher.identifyItem() - Cache Miss");
      }
    }

    Identifier identifier = providerToCache.identifyItem(localInfo);
    itemsDao.storeAsQuery(localInfo.getSurrogateName(), identifier);

    return identifier;
  }

  public Item loadItem(Identifier identifier, boolean bypassCache) {
    try {
      if(!bypassCache) {
        try {
          System.out.println("[FINE] CachedItemEnricher.enrichItem() - Loading from Cache: " + identifier);
          Item item = itemsDao.getItem(identifier);

          if(item.getVersion() < ItemsDao.VERSION) {
            System.out.println("[FINE] CachedItemEnricher.enrichItem() - Old version, updating from cached provider: " + item);

            item = providerToCache.loadItem(identifier);

            itemsDao.updateItem(item);  // TODO doubt this works, no id
          }

          System.out.println("[FINE] CachedItemEnricher.enrichItem() - Succesfully loaded: " + item);

          return item;
        }
        catch(ItemNotFoundException e) {
          System.out.println("[FINE] CachedItemEnricher.enrichItem() - Cache miss, falling back to cached provider: " + identifier);
        }
      }

      Item item = providerToCache.loadItem(identifier);

      itemsDao.storeItem(item);

      return item;
    }
    catch(Exception e) {
      System.out.println("[FINE] CachedItemEnricher.enrichItem() - Enrichment failed: " + e);
      throw new RuntimeException(e);
    }
  }
}
