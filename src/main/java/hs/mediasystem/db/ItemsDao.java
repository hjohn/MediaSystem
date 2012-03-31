package hs.mediasystem.db;

import hs.mediasystem.db.Database.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
public class ItemsDao {
  public static final int VERSION = 2;

  private final Provider<Connection> connectionProvider;
  private final Database database;
  private final PersonsDao personsDao;

  @Inject
  public ItemsDao(Database database, Provider<Connection> connectionProvider, PersonsDao personsDao) {
    this.database = database;
    this.connectionProvider = connectionProvider;
    this.personsDao = personsDao;

    Database.registerFetcher(new CastingsFetcher(connectionProvider));
  }

  private Connection getConnection() {
    return connectionProvider.get();
  }

  public Item getItem(final Identifier identifier) throws ItemNotFoundException {
    try {
      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("SELECT id, imdbid, title, subtitle, releasedate, rating, plot, runtime, version, season, episode, language, tagline, genres, backgroundurl, bannerurl, posterurl, background IS NOT NULL AS hasBackground, banner IS NOT NULL AS hasBanner, poster IS NOT NULL AS hasPoster FROM items WHERE type = ? AND provider = ? AND providerid = ?")) {
        statement.setString(1, identifier.getType());
        statement.setString(2, identifier.getProvider());
        statement.setString(3, identifier.getProviderId());

        System.out.println("[FINE] ItemsDao.getItem() - Selecting Item with type/provider/providerid = " + identifier.getType() + "/" + identifier.getProvider() + "/" + identifier.getProviderId());

        try(final ResultSet rs = statement.executeQuery()) {
          if(rs.next()) {
            return new Item() {{
              setIdentifier(identifier);
              setId(rs.getInt("id"));
              setImdbId(rs.getString("imdbid"));
              setTitle(rs.getString("title"));
              setSubtitle(rs.getString("subtitle"));
              setSeason(rs.getInt("season"));
              setEpisode(rs.getInt("episode"));
              setReleaseDate(rs.getDate("releasedate"));
              setPlot(rs.getString("plot"));
              setRating(rs.getFloat("rating"));
              setRuntime(rs.getInt("runtime"));
              setVersion(rs.getInt("version"));
              setLanguage(rs.getString("language"));
              setTagline(rs.getString("tagline"));
              setBackgroundURL(rs.getString("backgroundurl"));
              setBannerURL(rs.getString("bannerurl"));
              setPosterURL(rs.getString("posterurl"));

              if(getBackgroundURL() != null) {
                setBackground(new URLImageSource(connectionProvider, getId(), "items", "background", rs.getBoolean("hasBackground") ? null : getBackgroundURL()));
              }
              if(getBannerURL() != null) {
                setBanner(new URLImageSource(connectionProvider, getId(), "items", "banner", rs.getBoolean("hasBanner") ? null : getBannerURL()));
              }
              if(getPosterURL() != null) {
                setPoster(new URLImageSource(connectionProvider, getId(), "items", "poster", rs.getBoolean("hasPoster") ? null: getPosterURL()));
              }

              String genres = rs.getString("genres");
              setGenres(genres == null ? new String[] {} : genres.split(","));
            }};
          }
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
      Map<String, Object> generatedKeys = transaction.insert("items", createFieldMap(item));

      item.setId((int)generatedKeys.get("id"));
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
      throw new RuntimeException("exception while updating: " + item, e);
    }
  }

  private void storeCastings(Item item, Transaction transaction) throws SQLException {
    for(Casting casting : item.getCastings()) {
      Person person = personsDao.findByName(casting.getPerson().getName());

      if(person == null) {
        person = casting.getPerson();
        personsDao.store(person);
      }
      else {
        casting.setPerson(person);
      }

      transaction.insert("castings", createCastingsFieldMap(casting));
    }
  }

  private void putImagePlaceHolders(Item item) {
    int id = item.getId();

    item.setBackground(item.getBackgroundURL() == null ? null : new URLImageSource(connectionProvider, id, "items", "background", item.getBackgroundURL()));
    item.setBanner(item.getBannerURL() == null ? null : new URLImageSource(connectionProvider, id, "items", "banner", item.getBannerURL()));
    item.setPoster(item.getPosterURL() == null ? null : new URLImageSource(connectionProvider, id, "items", "poster", item.getPosterURL()));
  }

  public Identifier getQuery(String surrogateName) throws ItemNotFoundException {
    try {
      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("SELECT * FROM identifiers WHERE surrogatename = ?")) {
        statement.setString(1, surrogateName);

        System.out.println("[FINE] ItemsDao.getQuery() - Looking for Identifier with name: " + surrogateName);
        try(final ResultSet rs = statement.executeQuery()) {
          if(rs.next()) {
            return new Identifier(rs.getString("type"), rs.getString("provider"), rs.getString("providerid"));
          }
        }
      }

      throw new ItemNotFoundException(surrogateName);
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void storeAsQuery(String surrogateName, Identifier identifier) {
    try {
      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("DELETE FROM identifiers WHERE surrogatename = ?")) {
        statement.setString(1, surrogateName);
        statement.executeUpdate();
      }

      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("INSERT INTO identifiers (surrogatename, type, provider, providerid) VALUES (?, ?, ?, ?)")) {
        statement.setString(1, surrogateName);
        statement.setString(2, identifier.getType());
        statement.setString(3, identifier.getProvider());
        statement.setString(4, identifier.getProviderId());
        statement.execute();
      }
    }
    catch(SQLException e) {
      throw new RuntimeException("Exception while trying to store: " + surrogateName, e);
    }
  }

  private static Map<String, Object> createFieldMap(Item item) {
    Map<String, Object> columns = new LinkedHashMap<>();

    columns.put("imdbid", item.getImdbId());
    columns.put("title", item.getTitle());
    columns.put("subtitle", item.getSubtitle());
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

    columns.put("background", item.getBackground() == null ? null : item.getBackground().get());
    columns.put("banner", item.getBanner() == null ? null : item.getBanner().get());
    columns.put("poster", item.getPoster() == null ? null : item.getPoster().get());

    columns.put("type", item.getIdentifier().getType());
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
