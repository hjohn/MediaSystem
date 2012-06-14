package hs.mediasystem.framework;

import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichTask;

public abstract class AbstractEnrichTaskProvider<T> {
  private final String title;
  private final ItemsDao itemsDao;
  private final TypeBasedItemEnricher typeBasedItemEnricher;
  private final MediaData mediaData;

  public AbstractEnrichTaskProvider(String title, ItemsDao itemsDao, TypeBasedItemEnricher typeBasedItemEnricher, MediaData mediaData) {
    this.title = title;
    this.itemsDao = itemsDao;
    this.typeBasedItemEnricher = typeBasedItemEnricher;
    this.mediaData = mediaData;
  }

  public abstract T itemToEnrichType(Item item);

  public EnrichTask<T> getCachedTask() {
    return new EnrichTask<T>(true) {
      {
        updateTitle("Cache:" + title);
      }

      @Override
      protected T call() {
        try {
          if(mediaData.getIdentifier() == null) {
            return null;
          }

          Item item = itemsDao.loadItem(mediaData.getIdentifier());

          if(item.getVersion() < ItemsDao.VERSION) {
            return null;
          }

          return itemToEnrichType(item);
        }
        catch(ItemNotFoundException e) {
          return null;
        }
      }
    };
  }

  public EnrichTask<T> getTask(final boolean bypassCache) {
    return new EnrichTask<T>(false) {
      {
        updateTitle(title);
        updateProgress(0, 3);
      }

      @Override
      protected T call() throws Exception {
        if(mediaData.getIdentifier() == null) {
          return null;
        }

        Item oldItem;

        try {
          oldItem = itemsDao.loadItem(mediaData.getIdentifier());
        }
        catch(ItemNotFoundException e) {
          oldItem = null;
        }

        updateProgress(1, 3);

        Item item = bypassCache || oldItem == null ? typeBasedItemEnricher.loadItem(mediaData.getIdentifier()) : oldItem;

        updateProgress(2, 3);

        if(!item.equals(oldItem)) {
          if(oldItem != null) {
            item.setId(oldItem.getId());
            itemsDao.updateItem(item);
          }
          else {
            itemsDao.storeItem(item);
          }
        }

        updateProgress(3, 3);

        return itemToEnrichType(item);
      }
    };
  }
}