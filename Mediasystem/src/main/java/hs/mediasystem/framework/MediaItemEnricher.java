package hs.mediasystem.framework;

import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.dao.MediaId;
import hs.mediasystem.entity.Enricher;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityEnricher;
import hs.mediasystem.entity.EntitySource;
import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.util.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;

@EntityEnricher(entityClass = MediaItem.class, sourceClass = FileEntitySource.class, priority = 1.0)
public class MediaItemEnricher implements Enricher<MediaItem, Object> {
  private final DatabaseEntitySource databaseEntitySource;
  private final MediaDataDao mediaDataDao;
  private final Set<MediaItemIdentifier> mediaItemIdentifiers;
  private final SourceMatcher sourceMatcher;

  @Inject
  public MediaItemEnricher(DatabaseEntitySource databaseEntitySource, MediaDataDao mediaDataDao, Set<MediaItemIdentifier> mediaItemIdentifiers, SourceMatcher sourceMatcher) {
    this.databaseEntitySource = databaseEntitySource;
    this.mediaDataDao = mediaDataDao;
    this.mediaItemIdentifiers = mediaItemIdentifiers;
    this.sourceMatcher = sourceMatcher;
  }

  @Override
  public void enrich(EntityContext context, Task parent, MediaItem mediaItem, Object key) {
    List<Identifier> newIdentifiers = new ArrayList<>();

    hs.mediasystem.dao.MediaData dbMediaData = fetchMediaData(mediaItem);
    MediaData mediaData = context.add(MediaData.class, new SourceKey(databaseEntitySource, dbMediaData.getId()));

    /*
     * Store MediaData information queried from Database into Entity:
     */

    parent.addStep(context.getUpdateExecutor(), p -> {
      ObservableList<Identifier> identifiers = FXCollections.observableArrayList();

      for(hs.mediasystem.dao.Identifier dbIdentifier : dbMediaData.getIdentifiers()) {
        Identifier identifier = new Identifier();

        identifier.providerId.set(dbIdentifier.getProviderId());
        identifier.matchType.set(dbIdentifier.getMatchType());
        identifier.matchAccuracy.set(dbIdentifier.getMatchAccuracy());

        identifiers.add(identifier);
      }

      mediaData.setAll(
        dbMediaData.getUri(),
        dbMediaData.getMediaId() == null ? 0 : dbMediaData.getMediaId().getFileLength(),
        dbMediaData.getMediaId() == null ? null : dbMediaData.getMediaId().getOsHash(),
        dbMediaData.getResumePosition(),
        dbMediaData.isViewed(),
        identifiers
      );

      mediaItem.mediaData.set(mediaData);

      context.markClean(mediaItem.mediaData.get());
    });

    /*
     * If one or more Identifiers are missing or have expired, run the associated identification
     * steps:
     */

    // TODO only execute when needed, update condition:
    parent.addStep(context.getSlowExecutor(), () -> dbMediaData.getIdentifiers().isEmpty(), p -> {
      String mediaType = mediaItem.getDataType().getSimpleName();

      for(MediaItemIdentifier mediaItemIdentifier : mediaItemIdentifiers) {
        if(mediaItemIdentifier.getMediaType().equals(mediaType)) {
          Identifier identifier = mediaData.findIdentifier(mediaType, mediaItemIdentifier.getSource());

          if(identifier == null) { // TODO add lastupdated check here!
            try {
              identifier = mediaItemIdentifier.identify(mediaItem);
            }
            catch(RuntimeException e) {
              System.out.println("[WARN] " + getClass().getName() + ": " + mediaItemIdentifier + ": Error identifying: " + mediaItem);
              e.printStackTrace(System.out);
            }

            if(identifier != null) {
              newIdentifiers.add(identifier);
            }
            else {
              System.out.println("[INFO] " + getClass().getName() + ": " + mediaItemIdentifier + ": Identification failed (not found): " + mediaItem);
            }
          }
        }
      }
    });

    /*
     * Add any new identifiers to the Entity and finalize the enrichment of MediaItem by creating
     * the Media:
     */

    parent.addStep(context.getUpdateExecutor(), p -> {
      for(Identifier identifier : newIdentifiers) {
        mediaData.addIdentifier(identifier);
      }

      List<SourceKey> keys = new ArrayList<>();

      for(Identifier identifier : mediaItem.mediaData.get().identifiers.get()) {
        EntitySource source = sourceMatcher.sourceFromString(identifier.providerId.get().getProvider());

        if(source != null) {
          keys.add(new SourceKey(source, identifier.providerId.get().getId()));
        }
      }

      if(!keys.isEmpty()) {
        mediaItem.media.set(context.add(mediaItem.dataType.get(), keys.toArray(new SourceKey[keys.size()])));
      }
      else {
        System.out.println("[INFO] " + getClass().getName() + ": Unable to identify: " + mediaItem.getUri());
      }
    });
  }

  private hs.mediasystem.dao.MediaData fetchMediaData(MediaItem mediaItem) {
    hs.mediasystem.dao.MediaData mediaData = mediaDataDao.getMediaDataByUri(mediaItem.getUri());

    if(mediaData == null) {
      MediaId mediaId = MediaDataDao.createMediaId(mediaItem.getUri());

      mediaData = mediaDataDao.getMediaDataByHash(mediaId.getHash());

      if(mediaData == null) {
        mediaData = new hs.mediasystem.dao.MediaData();
      }

      mediaData.setUri(mediaItem.getUri());  // replace uri, as it didn't match anymore
      mediaData.setMediaId(mediaId);  // replace mediaId, even though hash matches, to update the other values just in case
      mediaData.setLastUpdated(new Date());

      mediaDataDao.storeMediaData(mediaData);
    }

    return mediaData;
  }
}
