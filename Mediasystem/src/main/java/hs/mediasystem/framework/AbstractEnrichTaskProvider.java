package hs.mediasystem.framework;

import hs.mediasystem.dao.Casting;
import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.enrich.EnrichTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractEnrichTaskProvider<T extends Media> {
  private final String title;
  private final ItemsDao itemsDao;
  private final MediaLoader mediaLoader;
  private final Identifier identifier;

  public AbstractEnrichTaskProvider(String title, ItemsDao itemsDao, MediaLoader itemEnricher, Identifier identifier) {
    this.title = title;
    this.itemsDao = itemsDao;
    this.mediaLoader = itemEnricher;
    this.identifier = identifier;
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
          if(identifier.getMediaType() == null) {
            return null;
          }

          Item item = itemsDao.loadItem(identifier);

          if(item.getVersion() < ItemsDao.VERSION) {
            return null;
          }

          T enrichType = itemToEnrichType(item);

          enrichType.castingsProperty().get().addAll(getOrderedCastings(item));

          return enrichType;
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
        updateProgress(0, 4);
      }

      @Override
      protected T call() throws Exception {
        if(identifier.getMediaType() == null) {
          return null;
        }

        Item oldItem;

        try {
          oldItem = itemsDao.loadItem(identifier);
        }
        catch(ItemNotFoundException e) {
          oldItem = null;
        }

        updateProgress(1, 4);

        Item item = bypassCache || oldItem == null ? mediaLoader.loadItem(identifier) : oldItem;

        updateProgress(2, 4);

        if(!item.equals(oldItem)) {
          if(oldItem != null) {
            item.setId(oldItem.getId());
            itemsDao.updateItem(item);
          }
          else {
            itemsDao.storeItem(item);
          }
        }

        updateProgress(3, 4);

        T enrichType = itemToEnrichType(item);

        enrichType.castingsProperty().get().addAll(getOrderedCastings(item));

        updateProgress(4, 4);

        return enrichType;
      }
    };
  }

  private static List<Casting> getOrderedCastings(Item item) {
    List<Casting> castings = new ArrayList<>(item.getCastings());

    Collections.sort(castings, new Comparator<Casting>() {
      @Override
      public int compare(Casting o1, Casting o2) {
        int result = Integer.compare(o1.getIndex(), o2.getIndex());

        if(result == 0) {
          String c1 = o1.getCharacterName() == null ? "" : o1.getCharacterName();
          String c2 = o2.getCharacterName() == null ? "" : o2.getCharacterName();

          result = c1.compareTo(c2);
        }

        return result;
      }
    });

    return castings;
  }
}