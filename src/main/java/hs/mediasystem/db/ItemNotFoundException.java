package hs.mediasystem.db;

public class ItemNotFoundException extends Exception {

  public ItemNotFoundException(Item item, Exception cause) {
    super(item.toString(), cause);
  }

  public ItemNotFoundException(Item item) {
    super(item.toString());
  }

  public ItemNotFoundException(Identifier identifier) {
    super(identifier.toString());
  }

  public ItemNotFoundException(String surrogateName) {
    super(surrogateName);
  }
}
