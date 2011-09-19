package hs.mediasystem.db;


public class CachedItemProvider implements ItemProvider {
  private final ItemsDao itemsDao;
  private final ItemProvider providerToCache;
  
  public CachedItemProvider(ItemProvider providerToCache) {
    this.providerToCache = providerToCache;
    
    try {
      itemsDao = new ItemsDao();
    }
    catch(ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Item getItem(Item item) throws ItemNotFoundException {
    String fileName = item.getPath().getFileName().toString();
    
    try {
      System.out.println("[FINE] Resolving from database cache: " + fileName);
      Item cachedItem = itemsDao.getItem(item.getPath());
      
      if(cachedItem.getVersion() < ItemsDao.VERSION) {
        System.out.println("[FINE] Old version, updating from cached provider: " + fileName);
        Item updatedItem = providerToCache.getItem(item);
        
        cachedItem.setBackground(updatedItem.getBackground());
        cachedItem.setPoster(updatedItem.getPoster());
        cachedItem.setBanner(updatedItem.getBanner());
        cachedItem.setImdbId(updatedItem.getImdbId());
        cachedItem.setPlot(updatedItem.getPlot());
        cachedItem.setTitle(updatedItem.getTitle());
        cachedItem.setRating(updatedItem.getRating());
        cachedItem.setReleaseDate(updatedItem.getReleaseDate());
        cachedItem.setRuntime(updatedItem.getRuntime());
        cachedItem.setSeason(updatedItem.getSeason());
        cachedItem.setEpisode(updatedItem.getEpisode());
        cachedItem.setType(updatedItem.getType());
        cachedItem.setSubtitle(updatedItem.getSubtitle());
        
        itemsDao.updateItem(cachedItem);
      }
      
      return cachedItem;
    }
    catch(ItemNotFoundException e) {
      System.out.println("[FINE] Cache miss, falling back to cached provider: " + fileName);
      Item cachedItem = providerToCache.getItem(item);
      
      itemsDao.storeItem(cachedItem);
      
      return cachedItem;
    }
  }
}
