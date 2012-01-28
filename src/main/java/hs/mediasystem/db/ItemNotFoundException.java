package hs.mediasystem.db;

public class ItemNotFoundException extends Exception {

  public ItemNotFoundException(Identifier identifier) {
    super(identifier.toString());
  }

  public ItemNotFoundException(String identifier, Exception cause) {
    super(identifier, cause);
  }

  public ItemNotFoundException(String surrogateName) {
    super(surrogateName);
  }
}
