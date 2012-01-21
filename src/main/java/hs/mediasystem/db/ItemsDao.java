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

  public Item getItem(Identifier identifier) throws ItemNotFoundException {
    try {
      try(Connection connection = getConnection();
          PreparedStatement statement = connection.prepareStatement("SELECT * FROM items WHERE type = ? AND provider = ? AND providerid = ?")) {
        statement.setString(1, identifier.getType().name());
        statement.setString(2, identifier.getProvider());
        statement.setString(3, identifier.getProviderId());

        System.out.println("[FINE] ItemsDao.getItem() - Selecting Item with type/provider/providerid = " + identifier.getType() + "/" + identifier.getProvider() + "/" + identifier.getProviderId());
        try(final ResultSet rs = statement.executeQuery()) {
          if(rs.next()) {
            return new Item(identifier) {{
              setId(rs.getInt("id"));
              setImdbId(rs.getString("imdbid"));
              setTitle(rs.getString("title"));
              setReleaseDate(rs.getDate("releasedate"));
              setRating(rs.getFloat("rating"));
              setPlot(rs.getString("plot"));
              setPoster(rs.getBytes("poster"));
              setBanner(rs.getBytes("banner"));
              setBackground(rs.getBytes("background"));
              setRuntime(rs.getInt("runtime"));
              setVersion(rs.getInt("version"));
              setSeason(rs.getInt("season"));
              setEpisode(rs.getInt("episode"));
              setSubtitle(rs.getString("subtitle"));
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
            return new Identifier(MediaType.valueOf(rs.getString("type")), rs.getString("provider"), rs.getString("providerid"));
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
        statement.setString(2, identifier.getType().name());
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
    columns.put("rating", item.getRating());
    columns.put("title", item.getTitle());
    columns.put("plot", item.getPlot());
    columns.put("poster", item.getPoster());
    columns.put("banner", item.getBanner());
    columns.put("background", item.getBackground());
    columns.put("lasthit", new Date());
    columns.put("lastupdated", new Date());
    columns.put("lastchecked", new Date());
    columns.put("releasedate", item.getReleaseDate());
    columns.put("runtime", item.getRuntime());
    columns.put("version", VERSION);
    columns.put("season", item.getSeason());
    columns.put("episode", item.getEpisode());
    columns.put("subtitle", item.getSubtitle());

    columns.put("type", item.getIdentifier().getType().name());
    columns.put("provider", item.getIdentifier().getProvider());
    columns.put("providerid", item.getIdentifier().getProviderId());

    return columns;
  }
}
