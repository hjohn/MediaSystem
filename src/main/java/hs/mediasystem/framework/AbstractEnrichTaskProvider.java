package hs.mediasystem.framework;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.TypeBasedItemEnricher;
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
      }

      @Override
      protected T call() throws Exception {
        Item oldItem;

        try {
          oldItem = itemsDao.loadItem(mediaData.getIdentifier());
        }
        catch(ItemNotFoundException e) {
          oldItem = null;
        }

        updateProgress(3, 5);

        Item item = bypassCache || oldItem == null ? typeBasedItemEnricher.loadItem(mediaData.getIdentifier()) : oldItem;

        updateProgress(4, 5);

        if(!item.equals(oldItem)) {
          if(oldItem != null) {
            item.setId(oldItem.getId());
            itemsDao.updateItem(item);
          }
          else {
            itemsDao.storeItem(item);
          }
        }

        updateProgress(5, 5);

        return itemToEnrichType(item);
      }
    };
  }
}