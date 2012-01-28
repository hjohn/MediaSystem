package hs.mediasystem.db;

import java.util.Map;

import javax.inject.Inject;

public class TypeBasedItemEnricher {
  private final Map<MediaType, ItemEnricher> itemEnrichers;

  @Inject
  public TypeBasedItemEnricher(Map<MediaType, ItemEnricher> itemEnrichers) {
    this.itemEnrichers = itemEnrichers;
  }

  public Identifier identifyItem(LocalInfo localInfo) throws IdentifyException {
    ItemEnricher itemEnricher = itemEnrichers.get(localInfo.getType());

    if(itemEnricher != null) {
      return new Identifier(localInfo.getType(), itemEnricher.getProviderCode(), itemEnricher.identifyItem(localInfo));
    }

    throw new IdentifyException("Could not identify " + localInfo + "; no matching enricher: " + localInfo.getType());
  }

  public Item loadItem(Identifier identifier) throws ItemNotFoundException {
    ItemEnricher itemEnricher = itemEnrichers.get(identifier.getType());

    if(itemEnricher == null) {
      throw new RuntimeException("No matching enricher for type: " + identifier.getType());
    }

    Item item = itemEnricher.loadItem(identifier.getProviderId());

    item.setIdentifier(identifier);

    return item;
  }
}
