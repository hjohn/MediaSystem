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
  public void enrichItem(Item item) throws ItemNotFoundException {
    ItemEnricher itemEnricher = itemEnrichers.get(item.getType());

    if(itemEnricher != null) {
      itemEnricher.enrichItem(item);
    }
  }
}
