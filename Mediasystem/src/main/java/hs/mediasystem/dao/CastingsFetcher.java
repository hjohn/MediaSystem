package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.Fetcher;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
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
      List<Object[]> results = transaction.select("role, charactername, index, p.id, p.name, p.biography, p.birthplace, p.birthdate, p.photourl", "castings c INNER JOIN persons p ON p.id = c.persons_id", "items_id = ?", item.getId());
      List<Casting> castings = new ArrayList<>();

      for(Object[] result : results) {
        Person person = new Person();

        person.setId((Integer)result[3]);
        person.setName((String)result[4]);
        person.setBiography((String)result[5]);
        person.setBirthPlace((String)result[6]);
        person.setBirthDate((Date)result[7]);
        person.setPhotoURL((String)result[8]);
        if(person.getPhotoURL() != null) {
          Object[] urlResult = transaction.selectUnique("url", "images", "url = ?", person.getPhotoURL());

          person.setPhoto(new DatabaseImageSource(transaction.getConnectionProvider(), person.getPhotoURL(), urlResult != null ? null : new URLImageSource(person.getPhotoURL())));
        }

        Casting casting = new Casting();

        casting.setItem(item);
        casting.setPerson(person);
        casting.setCharacterName((String)result[1]);
        casting.setIndex((Integer)result[2]);
        casting.setRole((String)result[0]);

        castings.add(casting);
      }

      return castings;
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
