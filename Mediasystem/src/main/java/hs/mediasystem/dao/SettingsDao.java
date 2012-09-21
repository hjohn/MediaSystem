package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SettingsDao {
  private final Database database;

  @Inject
  public SettingsDao(Database database) {
    this.database = database;
  }

  public List<Setting> getAllSettings() {
    try(Transaction transaction = database.beginReadOnlyTransaction()) {
      return transaction.select(Setting.class, null);
    }
  }

  public void storeSetting(Setting setting) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.merge(setting);
      transaction.commit();
    }
  }
}
