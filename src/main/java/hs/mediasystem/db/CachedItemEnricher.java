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
  public void identifyItem(Item item) throws ItemNotFoundException {
    System.out.println("[FINE] CachedItemEnricher.identifyItem() - with surrogatename: " + item.getSurrogateName());

    try {
      Item identifier = itemsDao.getQuery(item.getSurrogateName());

      System.out.println("[FINE] CachedItemEnricher.identifyItem() - Cache Hit");

      item.setType(identifier.getType());
      item.setProvider(identifier.getProvider());
      item.setProviderId(identifier.getProviderId());
    }
    catch(ItemNotFoundException e) {
      System.out.println("[FINE] CachedItemEnricher.identifyItem() - Cache Miss");
      providerToCache.identifyItem(item);
      itemsDao.storeAsQuery(item);
    }
  }

  @Override
  public void enrichItem(Item item) throws ItemNotFoundException {
    try {
      try {
        System.out.println("[FINE] Resolving from database cache: " + item);
        Item cachedItem = itemsDao.getItem(item);

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
          System.out.println("[FINE] Old version, updating from cached provider: " + item);

          providerToCache.enrichItem(item);
          itemsDao.updateItem(cachedItem);
        }

        System.out.println("[FINE] Succesfully enriched: " + item);
      }
      catch(ItemNotFoundException e) {
        System.out.println("[FINE] Cache miss, falling back to cached provider: " + item);

        providerToCache.enrichItem(item);

        System.out.println("[FINE] Storing newly enriched item: " + item);
        itemsDao.storeItem(item);
      }
    }
    catch(Exception e) {
      System.out.println("[WARN] Enrichment failed: " + e);
      throw new RuntimeException(e);
    }
  }
}
