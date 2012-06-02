package hs.mediasystem.media;

import hs.mediasystem.framework.MediaItem;

import java.lang.ref.WeakReference;

public class EnrichableDataObject {
  private WeakReference<MediaItem> mediaItem;

  public void setMediaItem(MediaItem mediaItem) {
    this.mediaItem = new WeakReference<>(mediaItem);
  }

  protected Class<? extends EnrichableDataObject> getEnrichClass() {
    return getClass();
  }

  protected void queueForEnrichment() {
    if(mediaItem != null) {
      MediaItem item = mediaItem.get();

      if(item != null) {
        item.queueForEnrichment(getEnrichClass());
      }
    }
  }
}
