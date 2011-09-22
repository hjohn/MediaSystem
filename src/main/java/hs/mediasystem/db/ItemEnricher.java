package hs.mediasystem.db;

public interface ItemEnricher {
  void enrichItem(Item item) throws ItemNotFoundException;
}
