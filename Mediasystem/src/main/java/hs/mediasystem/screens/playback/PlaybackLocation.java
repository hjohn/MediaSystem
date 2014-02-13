package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.Media;
import hs.mediasystem.screens.Location;

public class PlaybackLocation implements Location {
  private final Location parent;
  private final Media media;
  private final long startMillis;

  public PlaybackLocation(Location parent, Media media, long startMillis) {
    this.parent = parent;
    this.media = media;
    this.startMillis = startMillis;
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

  public Media getMedia() {
    return media;
  }

  public long getStartMillis() {
    return startMillis;
  }

  @Override
  public String getBreadCrumb() {
    return "Playback";
  }
}
