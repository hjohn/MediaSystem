package hs.mediasystem.screens.movie;

import hs.mediasystem.fs.NamedItem;

public class ItemUpdate {
  private final NamedItem namedItem;

  public ItemUpdate(NamedItem namedItem) {
    this.namedItem = namedItem;
  }

  public NamedItem getItem() {
    return namedItem;
  }
}
