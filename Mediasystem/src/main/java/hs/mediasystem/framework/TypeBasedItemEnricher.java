package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class TypeBasedItemEnricher {
  private static final Map<String, ItemEnricher> ITEM_ENRICHERS = new HashMap<>();

  public static void registerEnricher(Class<?> type, ItemEnricher itemEnricher) {
    ITEM_ENRICHERS.put(type.getSimpleName(), itemEnricher);
  }

  public Identifier identifyItem(Media media) throws IdentifyException {
    ItemEnricher itemEnricher = ITEM_ENRICHERS.get(media.getClass().getSimpleName());

    if(itemEnricher != null) {
      return itemEnricher.identifyItem(media);
    }

    throw new IdentifyException("Could not identify " + media + "; no matching enricher: " + media.getClass());
  }

  public Item loadItem(Identifier identifier) throws ItemNotFoundException {
    ItemEnricher itemEnricher = ITEM_ENRICHERS.get(identifier.getMediaType());

    if(itemEnricher == null) {
      throw new RuntimeException("No matching enricher for type: " + identifier.getMediaType());
    }

    Item item = itemEnricher.loadItem(identifier.getProviderId());

    item.setIdentifier(identifier);

    return item;
  }
}
