package hs.mediasystem.persist;

import javax.inject.Inject;
import javax.inject.Singleton;

import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;

@Singleton
public class Persister {
  private final ItemsDao itemsDao;

  @Inject
  public Persister(ItemsDao itemsDao) {
    this.itemsDao = itemsDao;
  }

  public void queueAsDirty(Persistable persistable) {
    if(persistable instanceof MediaData) {
      itemsDao.updateMediaData((MediaData)persistable);
    }
  }
}
