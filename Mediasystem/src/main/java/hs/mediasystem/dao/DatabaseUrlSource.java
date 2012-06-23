package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.Record;
import hs.mediasystem.util.WeakValueMap;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Provider;

public final class DatabaseUrlSource implements Source<byte[]> {
  private static final WeakValueMap<String, DatabaseUrlSource> INSTANCES = new WeakValueMap<>();

  private final Database database;
  private final String url;
  private final Source<byte[]> source;

  private boolean triedSource;

  /**
   * Constructs a new instance of this class, or returns null if url was null.
   *
   * @param connectionProvider a {@link Connection} {@link Provider}
   * @param url a url which serves as key
   * @throws SQLException when a database problem occurs
   */
  public static DatabaseUrlSource create(Database database, String url) throws SQLException {
    if(url == null) {
      return null;
    }

    synchronized(INSTANCES) {
      DatabaseUrlSource databaseImageSource = INSTANCES.get(url);

      if(databaseImageSource == null) {
        databaseImageSource = new DatabaseUrlSource(database, url);
        INSTANCES.put(url, databaseImageSource);
      }

      return databaseImageSource;
    }
  }

  private DatabaseUrlSource(Database database, String url) {
    assert database != null;
    assert url != null;

    this.database = database;
    this.url = url;

    try(Transaction transaction = database.beginTransaction()) {
      Record record = transaction.selectUnique("url", "images", "url = ?", url);

      this.source = record == null ? new URLImageSource(url) : null;
    }
  }

  private boolean isStoredInDatabase() {
    return source == null || triedSource;
  }

  @Override
  public synchronized byte[] get() {
    if(!isStoredInDatabase()) {
      triedSource = true;

      byte[] data = source.get();

      if(data != null) {
        storeData(data);
      }

      return data;
    }

    return getData();
  }

  private byte[] getData() {
    System.out.println("[FINE] DatabaseImageSource.getData() - Loading data '" + url + "'");

    try(Transaction transaction = database.beginTransaction()) {
      Image image = transaction.selectUnique(Image.class, "url = ?", url);

      if(image != null) {
        return image.getImage();
      }

      return null;
    }
  }

  private void storeData(byte[] data) {
    System.out.println("[FINE] DatabaseImageSource.storeData() - Storing data '" + url + "'");

    try(Transaction transaction = database.beginTransaction()) {
      transaction.insert(new Image(url, data));
      transaction.commit();
    }
  }

  @Override
  public boolean isLocal() {
    return isStoredInDatabase();
  }
}