package hs.mediasystem.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Provider;

public class DatabaseImageSource implements Source<byte[]> {
  private final Provider<Connection> connectionProvider;
  private final int id;
  private final Source<byte[]> source;
  private final String tableName;
  private final String columnName;

  private boolean triedSource;

  /**
   * Constructs a new instance of this class.
   *
   * @param connectionProvider a {@link Connection} {@link Provider}
   * @param id unique id that identifies a row in the given table
   * @param tableName the name of the table which contains the image
   * @param columnName the name of the column containing the raw image data
   * @param source a source where the image can be fetched from, or <code>null</code> if the image should be fetched only from the database
   */
  public DatabaseImageSource(Provider<Connection> connectionProvider, int id, String tableName, String columnName, Source<byte[]> source) {
    this.connectionProvider = connectionProvider;
    this.id = id;
    this.source = source;
    this.tableName = tableName;
    this.columnName = columnName;
  }

  private boolean isStoredInDatabase() {
    return source == null || triedSource;
  }

  @Override
  public synchronized byte[] get() {
    if(!isStoredInDatabase()) {
      triedSource = true;

      byte[] imageData = source.get();

      if(imageData != null) {
        storeImage(imageData);
      }

      return imageData;
    }

    return getImage();
  }

  private byte[] getImage() {
    System.out.println("[FINE] DatabaseImageSource.getImage() - Loading image " + tableName + "." + columnName + "(id=" + id + ")");

    try {
      try(Connection connection = connectionProvider.get();
          PreparedStatement statement = connection.prepareStatement("SELECT " + columnName + " FROM " + tableName + " WHERE id = ?")) {
        statement.setInt(1, id);

        try(ResultSet rs = statement.executeQuery()) {
          if(rs.next()) {
            return rs.getBytes(columnName);
          }
        }
      }

      return null;
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void storeImage(byte[] data) {
    System.out.println("[FINE] DatabaseImageSource.storeImage() - Storing image " + tableName + "." + columnName + "(id=" + id + ")");

    try {
      try(Connection connection = connectionProvider.get();
          PreparedStatement statement = connection.prepareStatement("UPDATE " + tableName + " SET " + columnName + "=? WHERE id = ?")) {
        statement.setObject(1, data);
        statement.setInt(2, id);

        statement.execute();
      }
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean isLocal() {
    return isStoredInDatabase();
  }
}