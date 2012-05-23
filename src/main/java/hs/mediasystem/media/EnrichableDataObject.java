package hs.mediasystem.media;

import hs.mediasystem.framework.MediaItem;

public class EnrichableDataObject {
  private MediaItem mediaItem;

  public void setMediaItem(MediaItem mediaItem) {
    this.mediaItem = mediaItem;
  }

  protected void queueForEnrichment() {
    if(mediaItem != null) {
      mediaItem.queueForEnrichment();
    }
  }
}
