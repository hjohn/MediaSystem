package hs.mediasystem.db;

public interface ItemEnricher {
  Identifier identifyItem(LocalInfo localInfo) throws IdentifyException;
  Item loadItem(Identifier identifier) throws ItemNotFoundException;
}
