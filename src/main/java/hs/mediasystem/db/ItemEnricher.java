package hs.mediasystem.db;

public interface ItemEnricher<T> {
  String getProviderCode();
  String identifyItem(LocalInfo<T> localInfo) throws IdentifyException;
  Item loadItem(String identifier, LocalInfo<T> localInfo) throws ItemNotFoundException;
}
