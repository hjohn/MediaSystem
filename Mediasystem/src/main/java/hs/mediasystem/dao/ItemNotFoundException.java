package hs.mediasystem.dao;


public class ItemNotFoundException extends Exception {

  public ItemNotFoundException(Identifier identifier) {
    super(identifier.toString());
  }

  public ItemNotFoundException(Identifier identifier, Exception cause) {
    super(identifier.toString(), cause);
  }

  public ItemNotFoundException(String message) {
    super(message);
  }
}
