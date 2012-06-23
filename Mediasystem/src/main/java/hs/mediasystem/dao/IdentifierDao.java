package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.DatabaseException;

import java.sql.SQLException;

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
    try(Transaction transaction = database.beginTransaction()) {
      return transaction.selectUnique(Identifier.class, "mediadata_id = ?", id);
    }
    catch(SQLException e) {
      throw new DatabaseException(e);
    }
  }

  public void storeIdentifier(Identifier identifier) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.merge(identifier);
      transaction.commit();
    }
    catch(SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
