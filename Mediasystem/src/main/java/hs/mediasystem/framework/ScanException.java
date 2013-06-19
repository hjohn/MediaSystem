package hs.mediasystem.framework;

public class ScanException extends RuntimeException {
  public ScanException(String message, Exception cause) {
    super(message, cause);
  }
}
