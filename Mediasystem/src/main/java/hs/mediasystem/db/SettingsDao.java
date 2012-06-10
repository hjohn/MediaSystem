package hs.mediasystem.db;

import hs.mediasystem.db.Database.Transaction;

import java.sql.SQLException;
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
    try(Transaction transaction = database.beginTransaction()) {
      return transaction.select(Setting.class, null);
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void storeSetting(Setting setting) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.merge(setting);
      transaction.commit();
    }
    catch(SQLException e) {
      throw new RuntimeException("exception while storing: " + setting, e);
    }
  }
}
