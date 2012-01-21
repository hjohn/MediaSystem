package hs.mediasystem.db;

public class ItemNotFoundException extends Exception {

  public ItemNotFoundException(Identifier item, Exception cause) {
    super(item.toString(), cause);
  }

  public ItemNotFoundException(Identifier identifier) {
    super(identifier.toString());
  }

  public ItemNotFoundException(String surrogateName) {
    super(surrogateName);
  }
}
