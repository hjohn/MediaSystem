package hs.mediasystem.db;

import java.util.HashMap;
import java.util.Map;

public class TypeBasedItemEnricher {
  private static final Map<String, ItemEnricher<?>> ITEM_ENRICHERS = new HashMap<>();

  public static void registerEnricher(String type, ItemEnricher<?> itemEnricher) {
    ITEM_ENRICHERS.put(type, itemEnricher);
  }

  public Identifier identifyItem(LocalInfo<Object> localInfo) throws IdentifyException {
    @SuppressWarnings("unchecked")
    ItemEnricher<Object> itemEnricher = (ItemEnricher<Object>)ITEM_ENRICHERS.get(localInfo.getType());

    if(itemEnricher != null) {
      return new Identifier(localInfo.getType(), itemEnricher.getProviderCode(), itemEnricher.identifyItem(localInfo));
    }

    throw new IdentifyException("Could not identify " + localInfo + "; no matching enricher: " + localInfo.getType());
  }

  public Item loadItem(Identifier identifier, LocalInfo<Object> localInfo) throws ItemNotFoundException {
    @SuppressWarnings("unchecked")
    ItemEnricher<Object> itemEnricher = (ItemEnricher<Object>) ITEM_ENRICHERS.get(identifier.getType());

    if(itemEnricher == null) {
      throw new RuntimeException("No matching enricher for type: " + identifier.getType());
    }

    Item item = itemEnricher.loadItem(identifier.getProviderId(), localInfo);

    item.setIdentifier(identifier);

    return item;
  }
}
