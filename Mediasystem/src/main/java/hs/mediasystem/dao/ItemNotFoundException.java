package hs.mediasystem.dao;

public class ItemNotFoundException extends Exception {

  public ItemNotFoundException(ProviderId providerId) {
    super(providerId.toString());
  }

  public ItemNotFoundException(ProviderId providerId, Exception cause) {
    super(providerId.toString(), cause);
  }

  public ItemNotFoundException(String message) {
    super(message);
  }
}
