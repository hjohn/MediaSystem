package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class IdentifierDao {
  private final Database database;

  @Inject
  public IdentifierDao(Database database) {
    this.database = database;
  }

  public Identifier getIdentifierByMediaDataId(int id) {
    try(Transaction transaction = database.beginReadOnlyTransaction()) {
      return transaction.selectUnique(Identifier.class, "mediadata_id = ?", id);
    }
  }

  public void storeIdentifier(Identifier identifier) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.merge(identifier);
      transaction.commit();
    }
  }
}
