package hs.mediasystem.util;

public interface Location {
  public enum Type {
    NORMAL, PLAYBACK;
  }

  String getId();
  String getBreadCrumb();
  Location getParent();
  Class<?> getParameterType();
  Type getType();
}
