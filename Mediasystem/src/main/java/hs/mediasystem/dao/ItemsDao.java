package hs.mediasystem.dao;

import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.DatabaseException;

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

    Database.registerFetcher(new CastingsFetcher(database));
  }

  private Connection getConnection() {
    return connectionProvider.get();
  }

  public Item loadItem(final Identifier identifier) throws ItemNotFoundException {
    try {
      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("SELECT id, imdbid, title, releasedate, rating, plot, runtime, version, season, episode, language, tagline, genres, backgroundurl, bannerurl, posterurl, EXISTS(SELECT url FROM images WHERE url = backgroundurl) AS hasBackground, EXISTS(SELECT url FROM images WHERE url = bannerurl) AS hasBanner, EXISTS(SELECT url FROM images WHERE url = posterurl) AS hasPoster FROM items WHERE type = ? AND provider = ? AND providerid = ?")) {
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
              setSeason((Integer)rs.getObject("season"));
              setEpisode((Integer)rs.getObject("episode"));
              setReleaseDate(rs.getDate("releasedate"));
              setPlot(rs.getString("plot"));
              setRating((Float)rs.getObject("rating"));
              setRuntime((Integer)rs.getObject("runtime"));
              setVersion(rs.getInt("version"));
              setLanguage(rs.getString("language"));
              setTagline(rs.getString("tagline"));
              setBackgroundURL(rs.getString("backgroundurl"));
              setBannerURL(rs.getString("bannerurl"));
              setPosterURL(rs.getString("posterurl"));

              if(getBackgroundURL() != null) {
                setBackground(new DatabaseImageSource(connectionProvider, getBackgroundURL(), rs.getBoolean("hasBackground") ? null : new URLImageSource(getBackgroundURL())));
              }
              if(getBannerURL() != null) {
                setBanner(new DatabaseImageSource(connectionProvider, getBannerURL(), rs.getBoolean("hasBanner") ? null : new URLImageSource((getBannerURL()))));
              }
              if(getPosterURL() != null) {
                setPoster(new DatabaseImageSource(connectionProvider, getPosterURL(), rs.getBoolean("hasPoster") ? null: new URLImageSource(getPosterURL())));
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
      throw new DatabaseException("exception while updating: " + item, e);
    }
  }

  private void storeCastings(Item item, Transaction transaction) throws SQLException {
    for(Casting casting : item.getCastings()) {
      Person person = personsDao.findByName(casting.getPerson().getName());

      if(person != null) {
        casting.getPerson().setId(person.getId());
        if(casting.getPerson().getPhotoURL() == null) {
          casting.getPerson().setPhotoURL(person.getPhotoURL());
        }
      }

      personsDao.merge(casting.getPerson());
      transaction.insert("castings", createCastingsFieldMap(casting));
    }
  }

  private void putImagePlaceHolders(Item item) {
    item.setBackground(item.getBackgroundURL() == null ? null : new DatabaseImageSource(connectionProvider, item.getBackgroundURL(), new URLImageSource(item.getBackgroundURL())));
    item.setBanner(item.getBannerURL() == null ? null : new DatabaseImageSource(connectionProvider, item.getBannerURL(), new URLImageSource(item.getBannerURL())));
    item.setPoster(item.getPosterURL() == null ? null : new DatabaseImageSource(connectionProvider, item.getPosterURL(), new URLImageSource(item.getPosterURL())));
  }

  public MediaData getMediaDataByUri(String uri) {
    try {
      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("SELECT * FROM mediadata WHERE uri = ?")) {
        statement.setString(1, uri);

        try(final ResultSet rs = statement.executeQuery()) {
          if(rs.next()) {
            return createMediaDataFromResultSet(rs);
          }
        }
      }

      return null;
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public MediaData getMediaDataByHash(byte[] hash) {
    try {
      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("SELECT * FROM mediadata WHERE hash = ?")) {
        statement.setBytes(1, hash);

        try(final ResultSet rs = statement.executeQuery()) {
          if(rs.next()) {
            return createMediaDataFromResultSet(rs);
          }
        }
      }

      return null;
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void updateMediaData(MediaData mediaData) {
    try(Transaction transaction = database.beginTransaction()) {
      Map<String, Object> parameters = createMediaDataFieldMap(mediaData);

      transaction.update("mediadata", mediaData.getId(), parameters);
      transaction.commit();
    }
    catch(SQLException e) {
      throw new DatabaseException("exception while updating: " + mediaData, e);
    }
  }

  public void storeMediaData(MediaData mediaData) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.delete("mediadata", "uri = ? or hash = ?", mediaData.getUri(), mediaData.getMediaId().getHash());

      Map<String, Object> generatedKeys = transaction.insert("mediadata", createMediaDataFieldMap(mediaData));

      mediaData.setId((int)generatedKeys.get("id"));
      transaction.commit();
    }
    catch(SQLException e) {
      throw new RuntimeException("exception while storing: " + mediaData, e);
    }
  }

  private MediaData createMediaDataFromResultSet(ResultSet rs) throws SQLException {
    MediaData data = new MediaData();

    data.setId(rs.getInt("id"));
    data.setLastUpdated(rs.getDate("lastupdated"));

    if(rs.getString("type") != null) {
      data.setIdentifier(new Identifier(rs.getString("type"), rs.getString("provider"), rs.getString("providerid"), MatchType.valueOf(rs.getString("matchtype")), rs.getFloat("matchaccuracy")));
    }
    data.setMediaId(new MediaId(rs.getLong("filelength"), rs.getLong("filetime"), rs.getLong("filecreatetime"), rs.getBytes("hash"), rs.getLong("oshash")));
    data.setUri(rs.getString("uri"));

    data.setResumePosition(rs.getInt("resumeposition"));
    data.setViewed(rs.getBoolean("viewed"));

    return data;
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

  private static Map<String, Object> createMediaDataFieldMap(MediaData mediaData) {
    Map<String, Object> columns = new LinkedHashMap<>();

    columns.put("lastupdated", mediaData.getLastUpdated());

    if(mediaData.getIdentifier() != null) {
      columns.put("type", mediaData.getIdentifier().getType());
      columns.put("provider", mediaData.getIdentifier().getProvider());
      columns.put("providerid", mediaData.getIdentifier().getProviderId());
      columns.put("matchtype", mediaData.getIdentifier().getMatchType().name());
      columns.put("matchaccuracy", mediaData.getIdentifier().getMatchAccuracy());
    }

    columns.put("uri", mediaData.getUri());

    columns.put("hash", mediaData.getMediaId().getHash());
    columns.put("oshash", mediaData.getMediaId().getOsHash());
    columns.put("filetime", mediaData.getMediaId().getFileTime());
    columns.put("filecreatetime", mediaData.getMediaId().getFileCreateTime());
    columns.put("filelength", mediaData.getMediaId().getFileLength());

    columns.put("resumeposition", mediaData.getResumePosition());
    columns.put("viewed", mediaData.isViewed());

    return columns;
  }
}