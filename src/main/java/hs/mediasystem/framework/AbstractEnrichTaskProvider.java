package hs.mediasystem.framework;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemNotFoundException;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichTask;
import hs.mediasystem.enrich.EnrichTaskProvider;
import hs.mediasystem.enrich.TaskKey;

public abstract class AbstractEnrichTaskProvider<T> implements EnrichTaskProvider<T> {
  private final ItemsDao itemsDao;
  private final TypeBasedItemEnricher typeBasedItemEnricher;
  private final TaskKey taskKey;
  private final MediaData mediaData;

  public AbstractEnrichTaskProvider(ItemsDao itemsDao, TypeBasedItemEnricher typeBasedItemEnricher, TaskKey taskKey, MediaData mediaData) {
    this.itemsDao = itemsDao;
    this.typeBasedItemEnricher = typeBasedItemEnricher;
    this.taskKey = taskKey;
    this.mediaData = mediaData;
  }

  public abstract T itemToEnrichType(Item item);

  @Override
  public EnrichTask<T> getCachedTask() {
    return new EnrichTask<T>(true) {
      {
        updateTitle("Cache:" + taskKey.getKey().getTitle());
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

  @Override
  public EnrichTask<T> getTask(final boolean bypassCache) {
    return new EnrichTask<T>(false) {
      {
        updateTitle(taskKey.getKey().getTitle());
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

  @Override
  public TaskKey getTaskKey() {
    return taskKey;
  }

  @Override
  public String toString() {
    return "AbstractEnricher[" + taskKey + "]";
  }
}