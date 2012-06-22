package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.Fetcher;
import hs.mediasystem.db.Record;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CastingsFetcher implements Fetcher<Item, Casting> {
  private final Database database;

  public CastingsFetcher(Database database) {
    this.database = database;
  }

  @Override
  public List<Casting> fetch(Item item) {
    if(item.getId() == 0) {
      return new ArrayList<>(0);
    }

    try(Transaction transaction = database.beginTransaction()) {
      List<Record> records = transaction.select("role, charactername, index, p.id, p.name, p.biography, p.birthplace, p.birthdate, p.photourl", "castings c INNER JOIN persons p ON p.id = c.persons_id", "items_id = ?", item.getId());
      List<Casting> castings = new ArrayList<>();

      for(Record record : records) {
        Person person = new Person();

        person.setId(record.getInteger("id"));
        person.setName(record.getString("name"));
        person.setBiography(record.getString("biography"));
        person.setBirthPlace(record.getString("birthplace"));
        person.setBirthDate(record.getDate("birthdate"));
        person.setPhotoURL(record.getString("photourl"));
        person.setPhoto(DatabaseUrlSource.create(database, person.getPhotoURL()));

        Casting casting = new Casting();

        casting.setItem(item);
        casting.setPerson(person);
        casting.setCharacterName(record.getString("charactername"));
        casting.setIndex(record.getInteger("index"));
        casting.setRole(record.getString("role"));

        castings.add(casting);
      }

      return castings;
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
