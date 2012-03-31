package hs.mediasystem.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

public class CastingsFetcher implements Fetcher<Item, Casting> {
  private final Provider<Connection> connectionProvider;

  public CastingsFetcher(Provider<Connection> connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  @Override
  public List<Casting> fetch(Item item) {
    if(item.getId() == 0) {
      return new ArrayList<>(0);
    }

    try(Connection connection = connectionProvider.get();
        PreparedStatement statement = connection.prepareStatement("SELECT role, charactername, index, p.id, p.name, p.photourl, p.photo IS NOT NULL AS hasPhoto FROM castings c INNER JOIN persons p ON p.id = c.persons_id WHERE items_id = ? ORDER BY index")) {
      statement.setInt(1, item.getId());

      List<Casting> castings = new ArrayList<>();

      try(ResultSet rs = statement.executeQuery()) {
        while(rs.next()) {
          Person person = new Person();

          person.setId(rs.getInt("id"));
          person.setName(rs.getString("name"));
          person.setPhotoURL(rs.getString("photourl"));
          person.setPhoto(new URLImageSource(connectionProvider, person.getId(), "person", "photo", rs.getBoolean("hasPhoto") ? null : person.getPhotoURL()));

          Casting casting = new Casting();

          casting.setItem(item);
          casting.setPerson(person);
          casting.setCharacterName(rs.getString("charactername"));
          casting.setIndex(rs.getInt("index"));
          casting.setRole(rs.getString("role"));

          castings.add(casting);
        }
      }

      return castings;
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
