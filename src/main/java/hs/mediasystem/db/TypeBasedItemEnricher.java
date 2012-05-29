package hs.mediasystem.db;

import hs.mediasystem.media.Media;

import java.util.HashMap;
import java.util.Map;

public class TypeBasedItemEnricher {
  private static final Map<String, ItemEnricher> ITEM_ENRICHERS = new HashMap<>();

  public static void registerEnricher(Class<?> type, ItemEnricher itemEnricher) {
    ITEM_ENRICHERS.put(type.getSimpleName(), itemEnricher);
  }

  public EnricherMatch identifyItem(Media media) throws IdentifyException {
    ItemEnricher itemEnricher = ITEM_ENRICHERS.get(media.getClass().getSimpleName());

    if(itemEnricher != null) {
      return itemEnricher.identifyItem(media);
    }

    throw new IdentifyException("Could not identify " + media + "; no matching enricher: " + media.getClass());
  }

  public Item loadItem(Identifier identifier) throws ItemNotFoundException {
    ItemEnricher itemEnricher = ITEM_ENRICHERS.get(identifier.getType());

    if(itemEnricher == null) {
      throw new RuntimeException("No matching enricher for type: " + identifier.getType());
    }

    Item item = itemEnricher.loadItem(identifier.getProviderId());

    item.setIdentifier(identifier);

    return item;
  }
}
