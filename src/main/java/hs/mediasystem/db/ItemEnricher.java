package hs.mediasystem.db;

public interface ItemEnricher {
  Identifier identifyItem(Item item) throws ItemNotFoundException;
  Item enrichItem(Item item, Identifier identifier) throws ItemNotFoundException;
}
