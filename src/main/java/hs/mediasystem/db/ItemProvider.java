package hs.mediasystem.db;

// TODO rename to something "enricher" as it enriches the data
public interface ItemProvider {
  Item getItem(Item item) throws ItemNotFoundException;
}
