package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;
import hs.mediasystem.db.Record;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
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
  }

  public MediaData getMediaDataByHash(byte[] hash) {
    try(Transaction transaction = database.beginTransaction()) {
      return createFromRecord(transaction.selectUnique("*", "mediadata", "hash = ?", hash));
    }
  }

  public void updateMediaData(MediaData mediaData) {
    try(Transaction transaction = database.beginTransaction()) {
      Map<String, Object> parameters = createMediaDataFieldMap(mediaData);

      transaction.update("mediadata", mediaData.getId(), parameters);
      transaction.commit();
    }
  }

  public void storeMediaData(MediaData mediaData) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.delete("mediadata", "uri = ? or hash = ?", mediaData.getUri(), mediaData.getMediaId().getHash());

      Object generatedKey = transaction.insert("mediadata", createMediaDataFieldMap(mediaData));

      mediaData.setId(((Number)generatedKey).intValue());
      transaction.commit();
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

  public static MediaId createMediaId(String uri) {
    long millis = System.currentTimeMillis();

    try {
      Path path = Paths.get(uri);
      boolean isDirectory = Files.isDirectory(path);

      MediaId mediaId = new MediaId(
        isDirectory ? 0 : Files.size(path),
        Files.getLastModifiedTime(path).toMillis(),
        ((FileTime)Files.getAttribute(path, "creationTime")).toMillis(),
        isDirectory ? null : MediaHash.loadMediaHash(path),
        isDirectory ? null : MediaHash.loadOpenSubtitlesHash(path)
      );

      System.out.println("[FINE] MediaDataEnricher.createMediaId() - computed MediaId in " + (System.currentTimeMillis() - millis) + " ms for: '" + uri + "'");

      return mediaId;
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
  }
}
