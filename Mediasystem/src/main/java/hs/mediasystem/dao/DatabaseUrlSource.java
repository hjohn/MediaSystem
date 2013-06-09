package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.Record;
import hs.mediasystem.util.WeakValueMap;

import java.sql.SQLException;

public final class DatabaseUrlSource implements Source<byte[]> {
  private static final WeakValueMap<String, DatabaseUrlSource> INSTANCES = new WeakValueMap<>();

  private final Database database;
  private final String url;

  private Source<byte[]> source;
  private boolean checkedSource;
  private boolean triedSource;

  /**
   * Constructs a new instance of this class, or returns null if url was null.
   *
   * @param database a {@link Database}
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
  }

  private Source<byte[]> getSource() {
    if(checkedSource) {
      return source;
    }

    checkedSource = true;

    try(Transaction transaction = database.beginReadOnlyTransaction()) {
      Record record = transaction.selectUnique("url", "images", "url = ?", url);

      source = record == null ? new URLImageSource(url) : null;
    }

    return source;
  }

  private boolean isStoredInDatabase() {
    return getSource() == null || triedSource;
  }

  @Override
  public synchronized byte[] get() {
    if(!isStoredInDatabase()) {
      triedSource = true;

      byte[] data = getSource().get();

      if(data != null) {
        storeData(data);
      }

      return data;
    }

    return getData();
  }

  private byte[] getData() {
    System.out.println("[FINE] DatabaseImageSource.getData() - Loading data '" + url + "'");

    try(Transaction transaction = database.beginReadOnlyTransaction()) {
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
  public synchronized boolean isLocal() {
    return isStoredInDatabase();
  }
}