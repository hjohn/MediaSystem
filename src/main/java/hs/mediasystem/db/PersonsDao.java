package hs.mediasystem.db;

import hs.mediasystem.db.Database.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    try(Connection connection = connectionProvider.get();
        PreparedStatement statement = connection.prepareStatement("SELECT id, name, photourl, photo IS NOT NULL AS hasPhoto FROM persons WHERE name = ?")) {
      statement.setString(1, name);

      try(ResultSet rs = statement.executeQuery()) {
        if(!rs.next()) {
          return null;
        }

        return new Person() {{
          setId(rs.getInt("id"));
          setName(rs.getString("name"));
          setPhotoURL(rs.getString("photourl"));
          if(getPhotoURL() != null) {
            setPhoto(new URLImageSource(connectionProvider, getId(), "persons", "photo", rs.getBoolean("hasPhoto") ? null : getPhotoURL()));
          }
        }};
      }
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

  private void putImagePlaceHolders(Person person) {
    int id = person.getId();

    person.setPhoto(person.getPhotoURL() == null ? null : new URLImageSource(connectionProvider, id, "persons", "photo", person.getPhotoURL()));
  }

  private static Map<String, Object> createFieldMap(Person person) {
    Map<String, Object> columns = new LinkedHashMap<>();

    columns.put("name", person.getName());
    columns.put("photourl", person.getPhotoURL());
    columns.put("photo", person.getPhoto() == null ? null : person.getPhoto().get());

    return columns;
  }
}
