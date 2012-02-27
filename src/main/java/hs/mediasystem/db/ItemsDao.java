package hs.mediasystem.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

  @Inject
  public ItemsDao(Provider<Connection> connectionProvider) {
    this.connectionProvider = connectionProvider;
  }

  private Connection getConnection() {
    return connectionProvider.get();
  }

  public enum ImageType {BACKGROUND, BANNER, POSTER}

  public byte[] getImage(int id, ImageType type) {
    System.out.println("[FINE] ItemsDao.getImage() - Loading image " + type + "(id=" + id + ")");

    try {
      String column = type.name().toLowerCase();

      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("SELECT " + column + " FROM items WHERE id = ?")) {
        statement.setInt(1, id);

        try(ResultSet rs = statement.executeQuery()) {
          if(rs.next()) {
            return rs.getBytes(column);
          }
        }
      }

      return null;
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public Item getItem(final Identifier identifier) throws ItemNotFoundException {
    try {
      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("SELECT id, imdbid, title, subtitle, releasedate, rating, plot, runtime, version, season, episode, language, tagline, genres, background IS NOT NULL AS hasBackground, banner IS NOT NULL AS hasBanner, poster IS NOT NULL AS hasPoster FROM items WHERE type = ? AND provider = ? AND providerid = ?")) {
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

              if(rs.getBoolean("hasBackground")) {
                setBackground(new ImageSource(getId(), ImageType.BACKGROUND));
              }
              if(rs.getBoolean("hasBanner")) {
                setBanner(new ImageSource(getId(), ImageType.BANNER));
              }
              if(rs.getBoolean("hasPoster")) {
                setPoster(new ImageSource(getId(), ImageType.POSTER));
              }

              String genres = rs.getString("genres");
              setGenres(genres == null ? new String[] {} : genres.split(","));
            }};
          }
        }
      }

      throw new ItemNotFoundException(identifier);
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void storeItem(Item item) {
    try {
      Map<String, Object> parameters = createFieldMap(item);

      StringBuilder fields = new StringBuilder();
      StringBuilder values = new StringBuilder();

      for(String key : parameters.keySet()) {
        if(fields.length() > 0) {
          fields.append(",");
          values.append(",");
        }

        fields.append(key);
        values.append("?");
      }

      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("INSERT INTO items (" + fields.toString() + ") VALUES (" + values.toString() + ")")) {
        setParameters(parameters, statement);
        statement.execute();
      }
    }
    catch(SQLException e) {
      throw new RuntimeException("Exception while trying to store: " + item, e);
    }
  }

  public void updateItem(Item item) {
    try {
      Map<String, Object> parameters = createFieldMap(item);

      StringBuilder set = new StringBuilder();

      for(String key : parameters.keySet()) {
        if(set.length() > 0) {
          set.append(",");
        }

        set.append(key);
        set.append("=?");
      }

      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("UPDATE items SET " + set.toString() + " WHERE id = ?")) {
        parameters.put("1", item.getId());

        System.out.println("[FINE] ItemsDao.updateItem() - Updating item with id: " + item.getId());

        setParameters(parameters, statement);
        statement.execute();
      }
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
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

  private static void setParameters(Map<String, Object> columns, PreparedStatement statement) throws SQLException {
    int parameterIndex = 1;

    for(String key : columns.keySet()) {
      Object value = columns.get(key);

      if(value instanceof Date) {
        statement.setTimestamp(parameterIndex++, new Timestamp(((Date)value).getTime()));
      }
      else {
        statement.setObject(parameterIndex++, value);
      }
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

    columns.put("background", item.getBackground().get());
    columns.put("banner", item.getBanner().get());
    columns.put("poster", item.getPoster().get());

    columns.put("type", item.getIdentifier().getType());
    columns.put("provider", item.getIdentifier().getProvider());
    columns.put("providerid", item.getIdentifier().getProviderId());

    return columns;
  }

  private class ImageSource implements Source<byte[]> {
    private final int id;
    private final ImageType type;

    public ImageSource(int id, ImageType type) {
      this.id = id;
      this.type = type;
    }

    @Override
    public byte[] get() {
      return ItemsDao.this.getImage(id, type);
    }
  }
}
