package hs.mediasystem.framework;

import hs.mediasystem.entity.EntityProvider;

public class MediaDataProvider implements EntityProvider<hs.mediasystem.dao.MediaData, MediaData> {

  @Override
  public MediaData get(hs.mediasystem.dao.MediaData mediaData) {
    return new MediaData(
      mediaData.getUri(),
      mediaData.getMediaId() == null ? 0 : mediaData.getMediaId().getFileLength(),
      mediaData.getMediaId() == null ? null : mediaData.getMediaId().getOsHash(),
      mediaData.getResumePosition(),
      mediaData.isViewed()
    );
  }

  @Override
  public Class<?> getType() {
    return MediaData.class;
  }
}