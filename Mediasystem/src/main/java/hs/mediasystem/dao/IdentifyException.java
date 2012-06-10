package hs.mediasystem.dao;

import hs.mediasystem.framework.Media;

public class IdentifyException extends Exception {

  public IdentifyException(Media media, Exception cause) {
    super(media.toString(), cause);
  }

  public IdentifyException(Media media) {
    super(media.toString());
  }

  public IdentifyException(String message) {
    super(message);
  }
}
