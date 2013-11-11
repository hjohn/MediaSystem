package hs.mediasystem.screens.playback;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.Location;

public class PlaybackLocation implements Location {
  private final Location parent;
  private final MediaItem mediaItem;
  private final long startMillis;

  public PlaybackLocation(Location parent, MediaItem mediaItem, long startMillis) {
    this.parent = parent;
    this.mediaItem = mediaItem;
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

  public MediaItem getMediaItem() {
    return mediaItem;
  }

  public long getStartMillis() {
    return startMillis;
  }

  @Override
  public String getBreadCrumb() {
    return "Playback";
  }
}
