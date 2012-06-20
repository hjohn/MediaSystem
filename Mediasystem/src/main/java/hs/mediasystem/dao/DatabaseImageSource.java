package hs.mediasystem.dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Provider;

public class DatabaseImageSource implements Source<byte[]> {
  private final Provider<Connection> connectionProvider;
  private final String url;
  private final Source<byte[]> source;

  private boolean triedSource;

  /**
   * Constructs a new instance of this class.
   *
   * @param connectionProvider a {@link Connection} {@link Provider}
   * @param source a source where the image can be fetched from, or <code>null</code> if the image should be fetched only from the database
   */
  public DatabaseImageSource(Provider<Connection> connectionProvider, String url, Source<byte[]> source) {
    assert connectionProvider != null;
    assert url != null;

    this.connectionProvider = connectionProvider;
    this.url = url;
    this.source = source;
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
    System.out.println("[FINE] DatabaseImageSource.getImage() - Loading image '" + url + "'");

    try {
      try(Connection connection = connectionProvider.get();
          PreparedStatement statement = connection.prepareStatement("SELECT image FROM images WHERE url = ?")) {
        statement.setString(1, url);

        try(ResultSet rs = statement.executeQuery()) {
          if(rs.next()) {
            return rs.getBytes("image");
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
    System.out.println("[FINE] DatabaseImageSource.storeImage() - Storing image '" + url + "'");

    try {
      try(Connection connection = connectionProvider.get();
          PreparedStatement statement = connection.prepareStatement("INSERT INTO images (url, image) VALUES (?, ?)")) {
        statement.setString(1, url);
        statement.setObject(2, data);

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