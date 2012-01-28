package hs.mediasystem.db;

public class IdentifyException extends Exception {

  public IdentifyException(LocalInfo localInfo, Exception cause) {
    super(localInfo.toString(), cause);
  }

  public IdentifyException(LocalInfo localInfo) {
    super(localInfo.toString());
  }

  public IdentifyException(String message) {
    super(message);
  }
}
