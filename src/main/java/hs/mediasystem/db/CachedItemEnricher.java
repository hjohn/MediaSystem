package hs.mediasystem.db;

import javax.inject.Inject;

public class CachedItemEnricher implements ItemEnricher {
  private final ItemsDao itemsDao;
  private final ItemEnricher providerToCache;

  @Inject
  public CachedItemEnricher(ItemsDao itemsDao, @Cachable ItemEnricher providerToCache) {
    this.itemsDao = itemsDao;
    this.providerToCache = providerToCache;
  }

  @Override
  public Identifier identifyItem(Item item) throws ItemNotFoundException {
    System.out.println("[FINE] CachedItemEnricher.identifyItem() - with surrogatename: " + item.getSurrogateName());

    if(!item.isBypassCache()) {
      try {
        Identifier identifier = itemsDao.getQuery(item.getSurrogateName());

        System.out.println("[FINE] CachedItemEnricher.identifyItem() - Cache Hit");

        return identifier;
      }
      catch(ItemNotFoundException e) {
        System.out.println("[FINE] CachedItemEnricher.identifyItem() - Cache Miss");
      }
    }

    Identifier identifier = providerToCache.identifyItem(item);
    itemsDao.storeAsQuery(item.getSurrogateName(), identifier);

    return identifier;
  }

  @Override
  public Item enrichItem(Item item, Identifier identifier) throws ItemNotFoundException {
    try {
      try {
        System.out.println("[FINE] CachedItemEnricher.enrichItem() - Loading from Cache: " + identifier);
        Item cachedItem = itemsDao.getItem(identifier);

        item.setId(cachedItem.getId());
        item.setVersion(cachedItem.getVersion());
        item.setBackground(cachedItem.getBackground());
        item.setPoster(cachedItem.getPoster());
        item.setBanner(cachedItem.getBanner());
        item.setImdbId(cachedItem.getImdbId());
        item.setPlot(cachedItem.getPlot());
        item.setTitle(cachedItem.getTitle());
        item.setRating(cachedItem.getRating());
        item.setReleaseDate(cachedItem.getReleaseDate());
        item.setRuntime(cachedItem.getRuntime());
        item.setSeason(cachedItem.getSeason());
        item.setEpisode(cachedItem.getEpisode());
        item.setType(cachedItem.getType());
        item.setSubtitle(cachedItem.getSubtitle());
        item.setProvider(cachedItem.getProvider());
        item.setProviderId(cachedItem.getProviderId());

        if(cachedItem.getVersion() < ItemsDao.VERSION) {
          System.out.println("[FINE] CachedItemEnricher.enrichItem() - Old version, updating from cached provider: " + item);

          providerToCache.identifyItem(item);
          providerToCache.enrichItem(item, identifier);
          itemsDao.updateItem(cachedItem);
        }

        System.out.println("[FINE] CachedItemEnricher.enrichItem() - Succesfully enriched: " + item);

        return item;
      }
      catch(ItemNotFoundException e) {
        System.out.println("[FINE] CachedItemEnricher.enrichItem() - Cache miss, falling back to cached provider: " + item);

        providerToCache.identifyItem(item);
        Item item2 = providerToCache.enrichItem(item, identifier);
        itemsDao.storeItem(item);

        return item2;
      }
    }
    catch(Exception e) {
      System.out.println("[FINE] CachedItemEnricher.enrichItem() - Enrichment failed: " + e);
      throw new RuntimeException(e);
    }
  }
}
