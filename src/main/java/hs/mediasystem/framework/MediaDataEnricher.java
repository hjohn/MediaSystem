package hs.mediasystem.framework;

import hs.mediasystem.db.EnricherMatch;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.MediaHash;
import hs.mediasystem.db.MediaId;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichTask;
import hs.mediasystem.enrich.Enricher;
import hs.mediasystem.enrich.EnrichmentState;
import hs.mediasystem.enrich.TaskKey;
import hs.mediasystem.media.Media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class MediaDataEnricher implements Enricher<MediaItem, MediaData> {
  private static final List<Class<?>> INPUT_PARAMETERS = new ArrayList<Class<?>>() {{
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
  public List<EnrichTask<MediaData>> enrich(MediaItem key, Map<Class<?>, Object> inputParameters, boolean bypassCache) {
    List<EnrichTask<MediaData>> enrichTasks = new ArrayList<>();

    MediaDataEnrichTaskProvider enrichTaskProvider = new MediaDataEnrichTaskProvider(new TaskKey(key, MediaData.class), key.getUri(), (Media)inputParameters.get(Media.class));

    if(!bypassCache) {
      enrichTasks.add(enrichTaskProvider.getCachedTask());
    }
    enrichTasks.add(enrichTaskProvider.getTask());

    return enrichTasks;
  }

  private static MediaId createMediaId(String uri) {
    try {
      Path path = Paths.get(uri);
      boolean isDirectory = Files.isDirectory(path);

      return new MediaId(
        isDirectory ? 0 : Files.size(path),
        Files.getLastModifiedTime(path).toMillis(),
        isDirectory ? null : MediaHash.loadMediaHash(path),
        isDirectory ? null : MediaHash.loadOpenSubtitlesHash(path)
      );
    }
    catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  private class MediaDataEnrichTaskProvider {
    private final TaskKey taskKey;
    private final String uri;
    private final Media media;

    private MediaDataEnrichTaskProvider(TaskKey taskKey, String uri, Media media) {
      this.taskKey = taskKey;
      this.uri = uri;
      this.media = media;
    }

    public EnrichTask<MediaData> getCachedTask() {
      return new EnrichTask<MediaData>(true) {
        {
          updateTitle("Cache:" + taskKey.getKey().getTitle());
        }

        @Override
        public MediaData call() throws IOException {
          MediaData mediaData = itemsDao.getMediaDataByUri(uri);

          if(mediaData == null) {
            MediaId mediaId = createMediaId(uri);

            taskKey.getKey().getEnrichCache().insert(taskKey.getKey(), EnrichmentState.ENRICHED, MediaId.class, mediaId);
            mediaData = itemsDao.getMediaDataByHash(mediaId.getHash());

            if(mediaData == null) {
              return null;
            }

            mediaData.setUri(uri);  // replace uri, as it didn't match anymore
            mediaData.setMediaId(mediaId);  // replace mediaId, even though hash matches, to update the other values just in case

            itemsDao.updateMediaData(mediaData);
          }

          return mediaData;
        }
      };
    }

    public EnrichTask<MediaData> getTask() {
      return new EnrichTask<MediaData>(false) {
        {
          updateTitle(taskKey.getKey().getTitle());
        }

        @Override
        protected MediaData call() throws Exception {
          EnricherMatch enricherMatch = typeBasedItemEnricher.identifyItem(media);

          MediaId mediaId = taskKey.getKey().getEnrichCache().getFromCache(taskKey.getKey(), MediaId.class);

          if(mediaId == null) {
            mediaId = createMediaId(uri);
          }

          MediaData mediaData = new MediaData();

          mediaData.setIdentifier(enricherMatch.getIdentifier());
          mediaData.setMediaId(mediaId);
          mediaData.setUri(uri);
          mediaData.setMatchType(enricherMatch.getMatchType());
          mediaData.setMatchAccuracy(enricherMatch.getMatchAccuracy());

          itemsDao.storeMediaData(mediaData);

          return mediaData;
        }
      };
    }
  }
}