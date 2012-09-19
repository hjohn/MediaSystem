package hs.mediasystem.framework;

import hs.mediasystem.dao.Item;
import hs.mediasystem.entity.EntityProvider;

public abstract class MediaProvider<T extends Media<T>> implements EntityProvider<Item, T> {
  @Override
  public final T get(Item item) {
    T media = createMedia(item);  // might be null, return null then

    if(media == null) {
      return media;
    }

    media.item.set(item);

    return media;
  }

  protected abstract T createMedia(Item item);
}