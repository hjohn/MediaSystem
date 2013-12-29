package hs.mediasystem.framework;

import hs.mediasystem.dao.Image;
import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;

import javax.inject.Inject;

public class DatabaseCache implements Cache {
  private final Database database;

  @Inject
  public DatabaseCache(Database database) {
    this.database = database;
  }

  @Override
  public byte[] lookup(String key) {
    try(Transaction transaction = database.beginReadOnlyTransaction()) {
      Image image = transaction.selectUnique(Image.class, "url=?", key);

      if(image != null) {
        return image.getImage();
      }

      return null;
    }
  }

  @Override
  public void store(String key, byte[] data) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.insert(new Image(key, data));
      transaction.commit();
    }
  }
}
