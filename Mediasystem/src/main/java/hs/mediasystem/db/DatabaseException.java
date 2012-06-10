package hs.mediasystem.db;

public class DatabaseException extends RuntimeException {

  public DatabaseException(String message, Throwable cause) {
    super(message, cause);
  }

  public DatabaseException(Throwable cause) {
    super(cause);
  }

  public DatabaseException(String message) {
    super(message);
  }

}
