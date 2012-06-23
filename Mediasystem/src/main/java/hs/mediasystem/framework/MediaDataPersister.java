package hs.mediasystem.framework;

import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.persist.PersistTask;
import hs.mediasystem.persist.Persister;

import javax.inject.Inject;

public class MediaDataPersister implements Persister<MediaData> {
  private final MediaDataDao mediaDataDao;
  private final PersistQueue queue;

  @Inject
  public MediaDataPersister(MediaDataDao mediaDataDao, PersistQueue queue) {
    this.mediaDataDao = mediaDataDao;
    this.queue = queue;
  }

  @Override
  public void queueAsDirty(final MediaData mediaData) {
    queue.queueAsDirty(mediaData, new PersistTask() {
      @Override
      public void persist() {
        mediaDataDao.updateMediaData(mediaData);
      }
    });
  }
}
