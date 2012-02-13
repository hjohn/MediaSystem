package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;

public class FilterItem {
  private final MediaItem mediaItem;
  private final String longText;
  private final String shortText;

  public FilterItem(MediaItem mediaItem, String longText, String shortText) {
    this.mediaItem = mediaItem;
    this.longText = longText;
    this.shortText = shortText;
  }

  public MediaItem getMediaItem() {
    return mediaItem;
  }

  public String getLongText() {
    return longText;
  }

  public String getShortText() {
    return shortText;
  }
}
