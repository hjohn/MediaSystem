package hs.mediasystem.screens.movie;

import hs.mediasystem.framework.MediaItem;

public class ItemUpdate {
  private final MediaItem namedItem;

  public ItemUpdate(MediaItem namedItem) {
    this.namedItem = namedItem;
  }

  public MediaItem getItem() {
    return namedItem;
  }
}
