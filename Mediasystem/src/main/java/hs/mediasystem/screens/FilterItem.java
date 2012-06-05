package hs.mediasystem.screens;

import hs.mediasystem.enrich.EnrichTrigger;

public class FilterItem {
  private final EnrichTrigger mediaItem;
  private final String longText;
  private final String shortText;

  public FilterItem(EnrichTrigger mediaItem, String longText, String shortText) {
    this.mediaItem = mediaItem;
    this.longText = longText;
    this.shortText = shortText;
  }

  public EnrichTrigger getMediaItem() {
    return mediaItem;
  }

  public String getLongText() {
    return longText;
  }

  public String getShortText() {
    return shortText;
  }
}
