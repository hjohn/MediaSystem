package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.DatabaseException;

import java.sql.SQLException;

import javax.inject.Inject;

public class PersonsDao {
  private final Database database;

  @Inject
  public PersonsDao(Database database) {
    this.database = database;
  }

  public Person findByName(String name) {
    try(Transaction transaction = database.beginTransaction()) {
      return transaction.selectUnique(Person.class, "name = ?", name);
    }
    catch(SQLException e) {
      throw new DatabaseException(e);
    }
  }
}
