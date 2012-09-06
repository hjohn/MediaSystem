package hs.mediasystem.screens;

import hs.mediasystem.util.Location;

public class PlaybackLocation implements Location {
  private final Location parent;

  public PlaybackLocation(Location parent) {
    this.parent = parent;
  }

  @Override
  public String getId() {
    return "Playback";
  }

  @Override
  public Location getParent() {
    return parent;
  }

  @Override
  public Class<?> getParameterType() {
    return null;
  }

  @Override
  public Type getType() {
    return Type.PLAYBACK;
  }

  @Override
  public String getBreadCrumb() {
    return "Playback";
  }
}
