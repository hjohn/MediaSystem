package hs.mediasystem.db;

public interface ItemEnricher {
  String getProviderCode();
  String identifyItem(LocalInfo localInfo) throws IdentifyException;
  Item loadItem(String identifier) throws ItemNotFoundException;
}
