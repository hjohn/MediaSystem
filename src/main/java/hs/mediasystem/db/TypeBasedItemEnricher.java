package hs.mediasystem.db;

import java.util.Map;

import javax.inject.Inject;

public class TypeBasedItemEnricher implements ItemEnricher {
  private final Map<String, ItemEnricher> itemEnrichers;

  @Inject
  public TypeBasedItemEnricher(Map<String, ItemEnricher> itemEnrichers) {
    this.itemEnrichers = itemEnrichers;
  }

  @Override
  public Identifier identifyItem(Item item) throws ItemNotFoundException {
    ItemEnricher itemEnricher = itemEnrichers.get(item.getType());

    if(itemEnricher != null) {
      return itemEnricher.identifyItem(item);
    }
    else {
      System.out.println("[FINE] TypeBasedItemEnricher.identifyItem() - No matching enricher for type: " + item.getType());
      return null;
    }
  }

  @Override
  public Item enrichItem(Item item, Identifier identifier) throws ItemNotFoundException {
    ItemEnricher itemEnricher = itemEnrichers.get(item.getType());

    if(itemEnricher == null) {
      throw new RuntimeException("No matching enricher for type: " + item.getType());
    }

    return itemEnricher.enrichItem(item, identifier);
  }
}
