package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.DatabaseException;
import hs.mediasystem.db.Record;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ItemsDao {
  public static final int VERSION = 2;

  private final Database database;
  private final PersonsDao personsDao;

  @Inject
  public ItemsDao(Database database, PersonsDao personsDao) {
    this.database = database;
    this.personsDao = personsDao;

    Database.registerFetcher(new CastingsFetcher(database));
  }

  public Item loadItem(final Identifier identifier) throws ItemNotFoundException {
    try {
      try(Transaction transaction = database.beginTransaction()) {
        final Record record = transaction.selectUnique(
          "id, imdbid, title, releasedate, rating, plot, runtime, version, season, episode, language, tagline, genres, backgroundurl, bannerurl, posterurl",
          "items",
          "type = ? AND provider = ? AND providerid = ?",
          identifier.getMediaType(), identifier.getProvider(), identifier.getProviderId()
        );

        System.out.println("[FINE] ItemsDao.getItem() - Selecting Item with type/provider/providerid = " + identifier.getMediaType() + "/" + identifier.getProvider() + "/" + identifier.getProviderId());

        if(record != null) {
          Item item = new Item(identifier);

          item.setId(record.getInteger("id"));
          item.setImdbId(record.getString("imdbid"));
          item.setTitle(record.getString("title"));
          item.setSeason(record.getInteger("season"));
          item.setEpisode(record.getInteger("episode"));
          item.setReleaseDate(record.getDate("releasedate"));
          item.setPlot(record.getString("plot"));
          item.setRating(record.getFloat("rating"));
          item.setRuntime(record.getInteger("runtime"));
          item.setVersion(record.getInteger("version"));
          item.setLanguage(record.getString("language"));
          item.setTagline(record.getString("tagline"));
          item.setBackgroundURL(record.getString("backgroundurl"));
          item.setBannerURL(record.getString("bannerurl"));
          item.setPosterURL(record.getString("posterurl"));

          item.setBackground(DatabaseUrlSource.create(database, item.getBackgroundURL()));
          item.setBanner(DatabaseUrlSource.create(database, item.getBannerURL()));
          item.setPoster(DatabaseUrlSource.create(database, item.getPosterURL()));

          String genres = record.getString("genres");
          item.setGenres(genres == null ? new String[] {} : genres.split(","));

          return item;
        }

        throw new ItemNotFoundException(identifier);
      }
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void storeItem(Item item) {
    try(Transaction transaction = database.beginTransaction()) {
      Object generatedKey = transaction.insert("items", createFieldMap(item));

      item.setId(((Number)generatedKey).intValue());
      putImagePlaceHolders(item);

      storeCastings(item, transaction);

      transaction.commit();
    }
    catch(SQLException e) {
      throw new RuntimeException("exception while storing: " + item, e);
    }
  }

  public void updateItem(Item item) {
    try(Transaction transaction = database.beginTransaction()) {
      Map<String, Object> parameters = createFieldMap(item);

      transaction.update("items", item.getId(), parameters);

      putImagePlaceHolders(item);

      if(item.isCastingsLoaded()) {
        transaction.deleteChildren("castings", "items", item.getId());

        storeCastings(item, transaction);
      }

      transaction.commit();
    }
    catch(SQLException e) {
      throw new DatabaseException("exception while updating: " + item, e);
    }
  }

  private void storeCastings(Item item, Transaction transaction) {
    for(Casting casting : item.getCastings()) {
      Person person = personsDao.findByName(casting.getPerson().getName());

      if(person != null) {
        casting.getPerson().setId(person.getId());
        if(casting.getPerson().getPhotoURL() == null) {
          casting.getPerson().setPhotoURL(person.getPhotoURL());
        }
      }

      transaction.merge(casting.getPerson());
      transaction.insert("castings", createCastingsFieldMap(casting));
    }
  }

  private void putImagePlaceHolders(Item item) throws SQLException {
    item.setBackground(DatabaseUrlSource.create(database, item.getBackgroundURL()));
    item.setBanner(DatabaseUrlSource.create(database, item.getBannerURL()));
    item.setPoster(DatabaseUrlSource.create(database, item.getPosterURL()));
  }



  private static Map<String, Object> createFieldMap(Item item) {
    Map<String, Object> columns = new LinkedHashMap<>();

    columns.put("imdbid", item.getImdbId());
    columns.put("title", item.getTitle());
    columns.put("season", item.getSeason());
    columns.put("episode", item.getEpisode());
    columns.put("releasedate", item.getReleaseDate());
    columns.put("plot", item.getPlot());
    columns.put("rating", item.getRating());
    columns.put("lasthit", new Date());
    columns.put("lastupdated", new Date());
    columns.put("lastchecked", new Date());
    columns.put("runtime", item.getRuntime());
    columns.put("version", VERSION);
    columns.put("language", item.getLanguage());
    columns.put("tagline", item.getTagline());

    String genres = "";

    for(String genre : item.getGenres()) {
      if(genre.length() > 0) {
        genres += ",";
      }
      genres += genre;
    }

    columns.put("genres", genres);

    columns.put("backgroundurl", item.getBackgroundURL());
    columns.put("bannerurl", item.getBannerURL());
    columns.put("posterurl", item.getPosterURL());

    columns.put("type", item.getIdentifier().getMediaType());
    columns.put("provider", item.getIdentifier().getProvider());
    columns.put("providerid", item.getIdentifier().getProviderId());

    return columns;
  }

  private static Map<String, Object> createCastingsFieldMap(Casting casting) {
    Map<String, Object> columns = new LinkedHashMap<>();

    columns.put("items_id", casting.getItem().getId());
    columns.put("persons_id", casting.getPerson().getId());
    columns.put("role", casting.getRole());
    columns.put("charactername", casting.getCharacterName());
    columns.put("index", casting.getIndex());

    return columns;
  }
}