package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.DatabaseException;
import hs.mediasystem.db.Record;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MediaDataDao {
  private final Database database;

  @Inject
  public MediaDataDao(Database database) {
    this.database = database;
  }

  public MediaData getMediaDataByUri(String uri) {
    try(Transaction transaction = database.beginTransaction()) {
      return createFromRecord(transaction.selectUnique("*", "mediadata", "uri = ?", uri));
    }
    catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public MediaData getMediaDataByHash(byte[] hash) {
    try(Transaction transaction = database.beginTransaction()) {
      return createFromRecord(transaction.selectUnique("*", "mediadata", "hash = ?", hash));
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

  private MediaData createFromRecord(Record record) {
    if(record == null) {
      return null;
    }

    MediaData data = new MediaData();

    data.setId(record.getInteger("id"));
    data.setLastUpdated(record.getDate("lastupdated"));

    data.setMediaId(new MediaId(record.getLong("filelength"), record.getLong("filetime"), record.getLong("filecreatetime"), record.getBytes("hash"), record.getLong("oshash")));
    data.setUri(record.getString("uri"));

    data.setResumePosition(record.getInteger("resumeposition"));
    data.setViewed(record.getBoolean("viewed"));

    return data;
  }

  private static Map<String, Object> createMediaDataFieldMap(MediaData mediaData) {
    Map<String, Object> columns = new LinkedHashMap<>();

    columns.put("lastupdated", mediaData.getLastUpdated());

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
