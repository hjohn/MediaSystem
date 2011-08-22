package hs.mediasystem.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class ItemsDao {
  public static final int VERSION = 2;

  private Connection connection;
  
  public ItemsDao() throws ClassNotFoundException {
    Class.forName("org.postgresql.Driver");
  }

  private Connection getConnection() throws SQLException {
    if(connection == null) {
      connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/mediasystem", "postgres", "W2rdpass");
      connection.prepareStatement("SET search_path = public").execute();
    }
    
    return connection;
  }
  
  public Item getItem(String fileName) throws ItemNotFoundException {
    try {
      Connection connection = getConnection();
      
      PreparedStatement statement = connection.prepareStatement("SELECT * FROM items WHERE localname = ?");
      
      statement.setString(1, fileName);
      
      final ResultSet rs = statement.executeQuery();
      
      if(rs.next()) {
        return new Item() {{
          setId(rs.getInt("id"));
          setLocalName(rs.getString("localname"));
          setImdbId(rs.getString("imdbid"));
          setTmdbId(rs.getString("tmdbid"));
          setTitle(rs.getString("title"));
          //setReleaseDate(rs.getDate("releasedate"));
          setRating(rs.getFloat("tmdbrating"));
          setPlot(rs.getString("plot"));
//          setCover(ImageIO.read(new ByteArrayInputStream(rs.getBytes("cover"))));
          setCover(rs.getBytes("cover"));
          setRuntime(rs.getInt("runtime"));
          setVersion(rs.getInt("version"));
        }};
      }
      
      throw new ItemNotFoundException();
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  public void storeItem(Item item) {
    try {
      Connection connection = getConnection();

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
      
      PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO items (" + fields.toString() + ") VALUES (" + values.toString() + ")"
      );

      setParameters(parameters, statement);
      statement.execute();
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public void updateItem(Item item) {
    try {
      Connection connection = getConnection();

      Map<String, Object> parameters = createFieldMap(item);
      
      StringBuilder set = new StringBuilder();
            
      for(String key : parameters.keySet()) {
        if(set.length() > 0) {
          set.append(",");
        }

        set.append(key);
        set.append("=?");
      }
      
      PreparedStatement statement = connection.prepareStatement(
        "UPDATE items SET " + set.toString() + " WHERE id = ?"
      );
      
      parameters.put("1", item.getId());

      System.out.println("Updating item with id: " + item.getId());
      
      setParameters(parameters, statement);
      statement.execute();
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void setParameters(Map<String, Object> columns, PreparedStatement statement) throws SQLException {
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

  private Map<String, Object> createFieldMap(Item item) {
    Map<String, Object> columns = new LinkedHashMap<String, Object>();
    
    columns.put("localname", item.getLocalName());
    columns.put("imdbid", item.getImdbId());
    columns.put("tmdbid", item.getTmdbId());
    columns.put("tmdbrating", item.getRating());
    columns.put("title", item.getTitle());
    columns.put("plot", item.getPlot());
    columns.put("cover", item.getCover());
    columns.put("lasthit", new Date());
    columns.put("lastupdated", new Date());
    columns.put("lastchecked", new Date());
    columns.put("releasedate", item.getReleaseDate());
    columns.put("runtime", item.getRuntime());
    columns.put("version", VERSION);
    
    return columns;
  }
}
