package hs.mediasystem.db;

import java.util.Map;

import javax.inject.Inject;

public class TypeBasedItemEnricher implements ItemEnricher {
  private final Map<MediaType, ItemEnricher> itemEnrichers;

  @Inject
  public TypeBasedItemEnricher(Map<MediaType, ItemEnricher> itemEnrichers) {
    this.itemEnrichers = itemEnrichers;
  }

  @Override
  public Identifier identifyItem(LocalInfo localInfo) throws IdentifyException {
    ItemEnricher itemEnricher = itemEnrichers.get(localInfo.getType());

    if(itemEnricher != null) {
      return itemEnricher.identifyItem(localInfo);
    }

    System.out.println("[FINE] TypeBasedItemEnricher.identifyItem() - No matching enricher for type: " + localInfo.getType());
    throw new IdentifyException(localInfo);
  }

  @Override
  public Item loadItem(Identifier identifier) throws ItemNotFoundException {
    ItemEnricher itemEnricher = itemEnrichers.get(identifier.getType());

    if(itemEnricher == null) {
      throw new RuntimeException("No matching enricher for type: " + identifier.getType());
    }

    return itemEnricher.loadItem(identifier);
  }
}
