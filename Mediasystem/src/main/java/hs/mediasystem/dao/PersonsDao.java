package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

public class PersonsDao {
  private final Provider<Connection> connectionProvider;
  private final Database database;

  @Inject
  public PersonsDao(Database database, Provider<Connection> connectionProvider) {
    this.database = database;
    this.connectionProvider = connectionProvider;
  }

  public Person findByName(String name) {
    try(Transaction transaction = database.beginTransaction()) {
      return transaction.selectUnique(Person.class, "name = ?", name);
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void store(Person person) {
    try(Transaction transaction = database.beginTransaction()) {
      Map<String, Object> generatedKeys = transaction.insert("persons", createFieldMap(person));

      person.setId((int)generatedKeys.get("id"));
      putImagePlaceHolders(person);

      transaction.commit();
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void merge(Person person) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.merge(person);

      putImagePlaceHolders(person);

      transaction.commit();
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void putImagePlaceHolders(Person person) {
    person.setPhoto(person.getPhotoURL() == null ? null : new DatabaseImageSource(connectionProvider, person.getPhotoURL(), new URLImageSource(person.getPhotoURL())));
  }

  private static Map<String, Object> createFieldMap(Person person) {
    Map<String, Object> columns = new LinkedHashMap<>();

    columns.put("name", person.getName());
    columns.put("biography", person.getBiography());
    columns.put("birthplace", person.getBirthPlace());
    columns.put("birthdate", person.getBirthDate());
    columns.put("photourl", person.getPhotoURL());
    columns.put("photo", person.getPhoto() == null ? null : person.getPhoto().get());

    return columns;
  }
}
