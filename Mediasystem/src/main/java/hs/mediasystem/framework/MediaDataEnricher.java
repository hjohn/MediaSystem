package hs.mediasystem.framework;

import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.dao.MediaHash;
import hs.mediasystem.dao.MediaId;
import hs.mediasystem.enrich.EnrichTask;
import hs.mediasystem.enrich.Enricher;
import hs.mediasystem.enrich.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MediaDataEnricher implements Enricher<MediaData> {
  private static final List<Class<?>> INPUT_PARAMETERS = new ArrayList<Class<?>>() {{
    add(TaskTitle.class);
    add(MediaItemUri.class);
  }};

  private final MediaDataDao mediaDataDao;

  @Inject
  public MediaDataEnricher(MediaDataDao mediaDataDao) {
    this.mediaDataDao = mediaDataDao;
  }

  @Override
  public List<Class<?>> getInputTypes() {
    return INPUT_PARAMETERS;
  }

  @Override
  public List<EnrichTask<MediaData>> enrich(Parameters parameters, boolean bypassCache) {
    List<EnrichTask<MediaData>> enrichTasks = new ArrayList<>();

    enrichTasks.add(createCachedTask(parameters.unwrap(TaskTitle.class), parameters.unwrap(MediaItemUri.class)));

    return enrichTasks;
  }

  private static MediaId createMediaId(String uri) {
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

  public EnrichTask<MediaData> createCachedTask(final String title, final String uri) {
    return new EnrichTask<MediaData>(true) {
      {
        updateTitle("Cache:" + title);
      }

      @Override
      public MediaData call() throws IOException {
        MediaData mediaData = mediaDataDao.getMediaDataByUri(uri);

        if(mediaData == null) {
          MediaId mediaId = createMediaId(uri);

          mediaData = mediaDataDao.getMediaDataByHash(mediaId.getHash());

          if(mediaData == null) {
            mediaData = new MediaData();
          }

          mediaData.setUri(uri);  // replace uri, as it didn't match anymore
          mediaData.setMediaId(mediaId);  // replace mediaId, even though hash matches, to update the other values just in case

          mediaDataDao.storeMediaData(mediaData);
        }

        return mediaData;
      }
    };
  }
}