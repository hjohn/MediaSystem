package hs.mediasystem.db;

import hs.mediasystem.screens.movie.Element;


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
  public Item getItem(String fileName, String title, String year, String imdbNumber) throws ItemNotFoundException {
    try {
      System.out.println("[FINE] Resolving from database cache: " + fileName);
      Item item = itemsDao.getItem(fileName);
      
      if(item.getVersion() < ItemsDao.VERSION) {
        System.out.println("[FINE] Old version, updating from cached provider: " + fileName);
        Item updatedItem = providerToCache.getItem(fileName, title, year, imdbNumber);
        
        item.setBackground(updatedItem.getBackground());
        item.setCover(updatedItem.getCover());
        item.setImdbId(updatedItem.getImdbId());
        item.setPlot(updatedItem.getPlot());
        item.setTitle(updatedItem.getTitle());
        item.setRating(updatedItem.getRating());
        item.setReleaseDate(updatedItem.getReleaseDate());
        item.setRuntime(updatedItem.getRuntime());
        
        itemsDao.updateItem(item);
      }
      
      return item;
    }
    catch(ItemNotFoundException e) {
      System.out.println("[FINE] Cache miss, falling back to cached provider: " + fileName);
      Item item = providerToCache.getItem(fileName, title, year, imdbNumber);
      
      itemsDao.storeItem(item);
      
      return item;
    }
  }

  @Override
  public Item getItem(Element element) throws ItemNotFoundException {
    String fileName = element.getPath().getFileName().toString();
    
    try {
      System.out.println("[FINE] Resolving from database cache: " + fileName);
      Item item = itemsDao.getItem(fileName);
      
      if(item.getVersion() < ItemsDao.VERSION) {
        System.out.println("[FINE] Old version, updating from cached provider: " + fileName);
        Item updatedItem = providerToCache.getItem(element);
        
        item.setBackground(updatedItem.getBackground());
        item.setCover(updatedItem.getCover());
        item.setImdbId(updatedItem.getImdbId());
        item.setPlot(updatedItem.getPlot());
        item.setTitle(updatedItem.getTitle());
        item.setRating(updatedItem.getRating());
        item.setReleaseDate(updatedItem.getReleaseDate());
        item.setRuntime(updatedItem.getRuntime());
        
        itemsDao.updateItem(item);
      }
      
      return item;
    }
    catch(ItemNotFoundException e) {
      System.out.println("[FINE] Cache miss, falling back to cached provider: " + fileName);
      Item item = providerToCache.getItem(element);
      
      itemsDao.storeItem(item);
      
      return item;
    }
  }
}
