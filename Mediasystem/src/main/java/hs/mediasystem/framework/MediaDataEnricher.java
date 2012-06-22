package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaData;
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
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

public class MediaDataEnricher implements Enricher<MediaData> {
  private static final List<Class<?>> INPUT_PARAMETERS = new ArrayList<Class<?>>() {{
    add(TaskTitle.class);
    add(MediaItemUri.class);
    add(Media.class);
  }};

  private final ItemsDao itemsDao;
  private final TypeBasedItemEnricher typeBasedItemEnricher;

  @Inject
  public MediaDataEnricher(ItemsDao itemsDao, TypeBasedItemEnricher typeBasedItemEnricher) {
    this.itemsDao = itemsDao;
    this.typeBasedItemEnricher = typeBasedItemEnricher;
  }

  @Override
  public List<Class<?>> getInputTypes() {
    return INPUT_PARAMETERS;
  }

  @Override
  public List<EnrichTask<MediaData>> enrich(Parameters parameters, boolean bypassCache) {
    List<EnrichTask<MediaData>> enrichTasks = new ArrayList<>();

    final AtomicReference<MediaId> mediaIdRef = new AtomicReference<>();

    if(!bypassCache) {
      enrichTasks.add(createCachedTask(parameters.unwrap(TaskTitle.class), parameters.unwrap(MediaItemUri.class), mediaIdRef));
    }
    enrichTasks.add(createTask(parameters.unwrap(TaskTitle.class), parameters.unwrap(MediaItemUri.class), parameters.get(Media.class), mediaIdRef));

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

  public EnrichTask<MediaData> createCachedTask(final String title, final String uri, final AtomicReference<MediaId> mediaIdRef) {
    return new EnrichTask<MediaData>(true) {
      {
        updateTitle("Cache:" + title);
      }

      @Override
      public MediaData call() throws IOException {
        MediaData mediaData = itemsDao.getMediaDataByUri(uri);

        if(mediaData == null) {
          MediaId mediaId = createMediaId(uri);

          mediaIdRef.set(mediaId);
          mediaData = itemsDao.getMediaDataByHash(mediaId.getHash());

          if(mediaData == null) {
            return null;
          }

          mediaData.setUri(uri);  // replace uri, as it didn't match anymore
          mediaData.setMediaId(mediaId);  // replace mediaId, even though hash matches, to update the other values just in case

          itemsDao.updateMediaData(mediaData);
        }

        /*
         * It's possible this MediaData does not contain an Identifier (negative caching), check last updated
         * and if more than a week old, return null.
         */

        if(mediaData.getIdentifier() == null && mediaData.getLastUpdated().getTime() < System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000) {
          return null;
        }

        return mediaData;
      }
    };
  }

  public EnrichTask<MediaData> createTask(final String title, final String uri, final Media media, final AtomicReference<MediaId> mediaIdRef) {
    return new EnrichTask<MediaData>(false) {
      {
        updateTitle(title);
        updateProgress(0, 3);
      }

      @Override
      protected MediaData call() throws Exception {
        Identifier identifier;

        try {
          identifier = typeBasedItemEnricher.identifyItem(media);
        }
        catch(IdentifyException e) {
          identifier = null;
        }

        updateProgress(1, 3);

        MediaId mediaId = mediaIdRef.get();

        if(mediaId == null) {
          mediaId = createMediaId(uri);
        }

        updateProgress(2, 3);

        MediaData mediaData = new MediaData();

        mediaData.setIdentifier(identifier);
        mediaData.setMediaId(mediaId);
        mediaData.setUri(uri);
        mediaData.setLastUpdated(new Date());

        itemsDao.storeMediaData(mediaData);

        updateProgress(3, 3);

        return mediaData;
      }
    };
  }
}