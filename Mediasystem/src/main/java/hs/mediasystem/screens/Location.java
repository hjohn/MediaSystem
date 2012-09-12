package hs.mediasystem.screens;

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
