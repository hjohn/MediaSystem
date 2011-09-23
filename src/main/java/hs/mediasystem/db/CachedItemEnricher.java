package hs.mediasystem.db;


public class CachedItemEnricher implements ItemEnricher {
  private final ItemsDao itemsDao;
  private final ItemEnricher providerToCache;
  
  public CachedItemEnricher(ItemEnricher providerToCache) {
    this.providerToCache = providerToCache;
    
    try {
      itemsDao = new ItemsDao();
    }
    catch(ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void enrichItem(Item item) throws ItemNotFoundException {
    String fileName = item.getPath().getFileName().toString();
    
    try {
      System.out.println("[FINE] Resolving from database cache: " + fileName);
      Item cachedItem = itemsDao.getItem(item.getPath());

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
        System.out.println("[FINE] Old version, updating from cached provider: " + fileName);
        
        providerToCache.enrichItem(item);
        itemsDao.updateItem(cachedItem);
      }
    }
    catch(ItemNotFoundException e) {
      System.out.println("[FINE] Cache miss, falling back to cached provider: " + fileName);

      providerToCache.enrichItem(item);
      itemsDao.storeItem(item);
    }
  }
}
