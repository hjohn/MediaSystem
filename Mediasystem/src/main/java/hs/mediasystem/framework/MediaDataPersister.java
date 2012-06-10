package hs.mediasystem.framework;

import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.persist.PersistTask;
import hs.mediasystem.persist.Persister;

import javax.inject.Inject;

public class MediaDataPersister implements Persister<MediaData> {
  private final ItemsDao itemsDao;
  private final PersistQueue queue;

  @Inject
  public MediaDataPersister(ItemsDao itemsDao, PersistQueue queue) {
    this.itemsDao = itemsDao;
    this.queue = queue;
  }

  @Override
  public void queueAsDirty(final MediaData mediaData) {
    queue.queueAsDirty(mediaData, new PersistTask() {
      @Override
      public void persist() {
        itemsDao.updateMediaData(mediaData);
      }
    });
  }
}
