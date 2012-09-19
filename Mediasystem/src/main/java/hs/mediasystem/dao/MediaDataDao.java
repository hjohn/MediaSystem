package hs.mediasystem.dao;

import hs.mediasystem.db.Database;
import hs.mediasystem.db.Database.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

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
      return transaction.selectUnique(MediaData.class, "uri=?", uri);
    }
  }

  public MediaData getMediaDataByHash(byte[] hash) {
    try(Transaction transaction = database.beginTransaction()) {
      return transaction.selectUnique(MediaData.class, "hash=?", hash);
    }
  }

  public void updateMediaData(MediaData mediaData) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.update(mediaData);
      transaction.commit();
    }
  }

  public void storeMediaData(MediaData mediaData) {
    try(Transaction transaction = database.beginTransaction()) {
      transaction.delete("mediadata", "uri = ? or hash = ?", mediaData.getUri(), mediaData.getMediaId().getHash());

      transaction.insert(mediaData);
      transaction.commit();
    }
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
