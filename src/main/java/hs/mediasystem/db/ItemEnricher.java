package hs.mediasystem.db;

public interface ItemEnricher {
  void identifyItem(Item item) throws ItemNotFoundException;
  void enrichItem(Item item) throws ItemNotFoundException;
}
