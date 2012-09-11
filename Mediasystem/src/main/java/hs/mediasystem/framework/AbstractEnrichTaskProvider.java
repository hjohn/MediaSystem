package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.enrich.EnrichTask;
import hs.mediasystem.screens.Casting;
import hs.mediasystem.screens.Person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractEnrichTaskProvider<T extends Media<?>> {
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
          if(identifier.getProviderId() == null) {
            return null;
          }

          Item item = itemsDao.loadItem(identifier.getProviderId());

          if(item.getVersion() < Item.VERSION) {
            return null;
          }

          T enrichType = itemToEnrichType(item);

          enrichType.castings.get().addAll(getOrderedCastings(item));

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
        if(identifier.getProviderId() == null) {
          return null;
        }

        Item oldItem;

        try {
          oldItem = itemsDao.loadItem(identifier.getProviderId());
        }
        catch(ItemNotFoundException e) {
          oldItem = null;
        }

        updateProgress(1, 4);

        Item item = bypassCache || oldItem == null ? mediaLoader.loadItem(identifier.getProviderId()) : oldItem;

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

        enrichType.castings.get().addAll(getOrderedCastings(item));

        updateProgress(4, 4);

        return enrichType;
      }
    };
  }

  private static List<Casting> getOrderedCastings(Item item) {
    List<hs.mediasystem.dao.Casting> castings = new ArrayList<>(item.getCastings());

    Collections.sort(castings, new Comparator<hs.mediasystem.dao.Casting>() {
      @Override
      public int compare(hs.mediasystem.dao.Casting o1, hs.mediasystem.dao.Casting o2) {
        int result = Integer.compare(o1.getIndex(), o2.getIndex());

        if(result == 0) {
          String c1 = o1.getCharacterName() == null ? "" : o1.getCharacterName();
          String c2 = o2.getCharacterName() == null ? "" : o2.getCharacterName();

          result = c1.compareTo(c2);
        }

        return result;
      }
    });

    List<Casting> result = new ArrayList<>();

    for(final hs.mediasystem.dao.Casting casting : castings) {
      Person p = new Person();

      p.personRecord.set(casting.getPerson());

      Casting c = new Casting();

      c.person.set(p);
      c.role.set(casting.getRole());
      c.characterName.set(casting.getCharacterName());
      c.index.set(casting.getIndex());

      result.add(c);
    }

    return result;
  }
}