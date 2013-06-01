package hs.mediasystem.screens;

import javax.inject.Provider;

public interface MediaNodeCellProvider extends Provider<MediaNodeCell> {
  public enum Type { HORIZONTAL, VERTICAL, SQUARE }

  Class<?> getMediaType();
  Type getType();
}
