package hs.mediasystem.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Provider;

public class URLImageSource implements Source<byte[]> {
  private final Provider<Connection> connectionProvider;
  private final int id;
  private final String url;
  private final String tableName;
  private final String columnName;

  private boolean triedDownloading;

  /**
   * Constructs a new instance of this class.
   *
   * @param connectionProvider a {@link Connection} {@link Provider}
   * @param id unique id that identifies a row in the given table
   * @param tableName the name of the table which contains the image
   * @param columnName the name of the column containing the raw image data
   * @param url a URL where the image can be fetched from, or <code>null</code> if the image should be fetched only from the database
   */
  public URLImageSource(Provider<Connection> connectionProvider, int id, String tableName, String columnName, String url) {
    this.connectionProvider = connectionProvider;
    this.id = id;
    this.url = url;
    this.tableName = tableName;
    this.columnName = columnName;
  }

  @Override
  public byte[] get() {
    if(url != null && !triedDownloading) {
      triedDownloading = true;

      byte[] imageData = Downloader.tryReadURL(url);

      if(imageData != null) {
        storeImage(imageData);
      }

      return imageData;
    }

    return getImage();
  }

  private byte[] getImage() {
    System.out.println("[FINE] URLImageSource.getImage() - Loading image " + tableName + "." + columnName + "(id=" + id + ")");

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
    System.out.println("[FINE] URLImageSource.storeImage() - Storing image " + tableName + "." + columnName + "(id=" + id + ")");

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
}