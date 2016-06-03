package hs.mediasystem.framework;

import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.dao.MediaId;
import hs.mediasystem.entity.Enricher;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityEnricher;
import hs.mediasystem.entity.EntitySource;
import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.util.Throwables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;

@EntityEnricher(entityClass = Media.class, sourceClass = FileEntitySource.class, priority = 1.0)
public class MediaEnricher implements Enricher<Media, Object> {
  private final DatabaseEntitySource databaseEntitySource;
  private final MediaDataDao mediaDataDao;
  private final Set<MediaIdentifier<?>> mediaItemIdentifiers;
  private final SourceMatcher sourceMatcher;

  @Inject
  public MediaEnricher(DatabaseEntitySource databaseEntitySource, MediaDataDao mediaDataDao, Set<MediaIdentifier<?>> mediaItemIdentifiers, SourceMatcher sourceMatcher) {
    this.databaseEntitySource = databaseEntitySource;
    this.mediaDataDao = mediaDataDao;
    this.mediaItemIdentifiers = mediaItemIdentifiers;
    this.sourceMatcher = sourceMatcher;
  }

  @Override
  public CompletableFuture<Void> enrich(EntityContext context, Media media, Object key) {
    if(media.mediaItem.get().mediaData.get() != null) {
      return optionallyIdentifyAndFinalize(context, media);
    }

    return CompletableFuture
      .completedFuture(fetchMediaData(media))
      .thenAcceptAsync(dbMediaData -> updateEntity(context, media, dbMediaData), context.getUpdateExecutor())
      .thenCompose(v -> optionallyIdentifyAndFinalize(context, media));
  }

  private hs.mediasystem.dao.MediaData fetchMediaData(Media media) {
    MediaItem mediaItem = media.getMediaItem();
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

  private void updateEntity(EntityContext context, Media media, hs.mediasystem.dao.MediaData dbMediaData) {
    MediaData mediaData = context.add(MediaData.class, new SourceKey(databaseEntitySource, dbMediaData.getId()));

    /*
     * Store MediaData information queried from Database into Entity:
     */

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

    media.getMediaItem().mediaData.set(mediaData);

    context.markClean(media.getMediaItem().mediaData.get());
  }

  private CompletableFuture<Void> optionallyIdentifyAndFinalize(EntityContext context, Media media) {
    CompletableFuture<List<Identifier>> currentStage = CompletableFuture.completedFuture(Collections.emptyList());
    MediaData mediaData = media.getMediaItem().mediaData.get();

    for(MediaIdentifier<?> mediaItemIdentifier : mediaItemIdentifiers) {
      if(mediaData.findIdentifier(mediaItemIdentifier.getMediaType(), mediaItemIdentifier.getSource()) == null) {

        /*
         * If one or more Identifiers are missing or have expired, run the associated identification
         * steps:
         */

        currentStage = CompletableFuture.supplyAsync(() -> identify(media), context.getSlowExecutor());
        break;
      }
    }

    return currentStage.thenAcceptAsync(newIdentifiers -> finalizeEnrichment(context, media, newIdentifiers), context.getUpdateExecutor());
  }

  private List<Identifier> identify(Media media) {
    MediaData mediaData = media.getMediaItem().mediaData.get();
    List<Identifier> newIdentifiers = new ArrayList<>();
    String mediaType = media.getClass().getSimpleName();

    for(MediaIdentifier<?> mediaIdentifier : mediaItemIdentifiers) {
      if(mediaIdentifier.getMediaType().equals(mediaType)) {
        Identifier identifier = mediaData.findIdentifier(mediaType, mediaIdentifier.getSource());

        if(identifier == null) { // TODO add lastupdated check here!
          try {
            @SuppressWarnings("unchecked")
            MediaIdentifier<Media> castedMediaItemIdentifier = (MediaIdentifier<Media>)mediaIdentifier;

            identifier = castedMediaItemIdentifier.identify(media);
          }
          catch(RuntimeException e) {
            System.out.println("[WARN] " + getClass().getName() + "::identify - " + mediaIdentifier + ": Error identifying: " + media + ": " + Throwables.formatAsOneLine(e));
          }

          if(identifier != null) {
            newIdentifiers.add(identifier);
          }
          else {
            System.out.println("[INFO] " + getClass().getName() + "::identify - " + mediaIdentifier + ": Identification failed (not found): " + media);
          }
        }
      }
    }

    return newIdentifiers;
  }

  /**
   * Add any new identifiers to the Entity and finalize the enrichment of Media by
   * associating it with the new keys.
   */
  private void finalizeEnrichment(EntityContext context, Media media, List<Identifier> newIdentifiers) {
    MediaData mediaData = media.getMediaItem().mediaData.get();

    for(Identifier identifier : newIdentifiers) {
      mediaData.addIdentifier(identifier);
    }

    List<SourceKey> keys = new ArrayList<>();

    for(Identifier identifier : mediaData.identifiers.get()) {
      EntitySource source = sourceMatcher.sourceFromString(identifier.providerId.get().getProvider());

      if(source != null) {
        keys.add(new SourceKey(source, identifier.providerId.get().getId()));
      }
    }

    if(!keys.isEmpty()) {
      context.associate(media, keys.toArray(new SourceKey[keys.size()]));
    }
    else {
      System.out.println("[INFO] " + getClass().getName() + "::finalizeEnrichment - Unable to identify: " + media.getMediaItem().getUri());
    }
  }
}
