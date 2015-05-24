package hs.mediasystem.framework;

import hs.mediasystem.dao.Image;
import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;

import java.time.LocalDateTime;

import javax.inject.Inject;
import javax.inject.Named;

public class DatabaseCache implements Cache {
  private final Database database;
  private final int maxAgeInSeconds;

  @Inject
  public DatabaseCache(Database database, @Named("DatabaseCache.expirationSeconds") int maxAgeInSeconds) {
    this.database = database;
    this.maxAgeInSeconds = maxAgeInSeconds;
  }

  @Override
  public byte[] lookup(String key) {
    try(Transaction transaction = database.beginReadOnlyTransaction()) {
      Image image = transaction.selectUnique(Image.class, "url=?", key);

      LocalDateTime oldestAllowed = LocalDateTime.now().minusSeconds(maxAgeInSeconds);

      if(image != null && image.getCreationTime().isAfter(oldestAllowed)) {
        return image.getImage();
      }

      return null;
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
