package hs.mediasystem.framework;

import hs.mediasystem.dao.Image;
import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;

import javax.inject.Inject;

public class DatabaseCache implements Cache<byte[]> {
  private final Database database;

  @Inject
  public DatabaseCache(Database database) {
    this.database = database;
  }

  @Override
  public CacheEntry<byte[]> lookup(String key) {
    try(Transaction transaction = database.beginReadOnlyTransaction()) {
      Image image = transaction.selectUnique(Image.class, "url=?", key);

      return image == null ? null : new CacheEntry<>(image.getImage(), image.getCreationTime());
    }
  }

  @Override
  public void store(String key, byte[] data) {
    try(Transaction transaction = database.beginTransaction()) {
      if(transaction.selectUnique(Image.class, "url=?", key) != null) {
        transaction.update(new Image(key, data));
      }
      else {
        transaction.insert(new Image(key, data));
      }
      transaction.commit();
    }
  }
}
