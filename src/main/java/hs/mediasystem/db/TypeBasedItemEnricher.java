package hs.mediasystem.db;

import hs.mediasystem.framework.MediaItem;

import java.util.HashMap;
import java.util.Map;

public class TypeBasedItemEnricher {
  private static final Map<String, ItemEnricher> ITEM_ENRICHERS = new HashMap<>();

  public static void registerEnricher(String type, ItemEnricher itemEnricher) {
    ITEM_ENRICHERS.put(type, itemEnricher);
  }

  public Identifier identifyItem(MediaItem mediaItem) throws IdentifyException {
    ItemEnricher itemEnricher = ITEM_ENRICHERS.get(mediaItem.getMediaType());

    if(itemEnricher != null) {
      return new Identifier(mediaItem.getMediaType(), itemEnricher.getProviderCode(), itemEnricher.identifyItem(mediaItem));
    }

    throw new IdentifyException("Could not identify " + mediaItem + "; no matching enricher: " + mediaItem.getMediaType());
  }

  public Item loadItem(Identifier identifier, MediaItem mediaItem) throws ItemNotFoundException {
    ItemEnricher itemEnricher = ITEM_ENRICHERS.get(identifier.getType());

    if(itemEnricher == null) {
      throw new RuntimeException("No matching enricher for type: " + identifier.getType());
    }

    Item item = itemEnricher.loadItem(identifier.getProviderId(), mediaItem);

    item.setIdentifier(identifier);

    return item;
  }
}
