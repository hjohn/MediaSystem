package hs.mediasystem.framework;

import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.dao.MediaId;
import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.EntityFactory;
import hs.mediasystem.entity.FinishEnrichCallback;

import java.util.Date;

import javax.inject.Inject;

public class MediaItemConfigurator {
  private final IdentifierDao identifierDao;
  private final MediaDataDao mediaDataDao;
  private final EntityFactory<DatabaseObject> entityFactory;

  @Inject
  public MediaItemConfigurator(IdentifierDao identifierDao, MediaDataDao mediaDataDao, EntityFactory<DatabaseObject> entityFactory) {
    this.identifierDao = identifierDao;
    this.mediaDataDao = mediaDataDao;
    this.entityFactory = entityFactory;
  }

  public void configure(final MediaItem mediaItem, final MediaIdentifier mediaIdentifier) {
    mediaItem.mediaData.setEnricher(new EnricherBuilder<MediaItem, MediaData>(MediaData.class)
      .enrich(new EnrichCallback<MediaData>() {
        @Override
        public MediaData enrich(Object... parameters) {
          hs.mediasystem.dao.MediaData dbMediaData = mediaDataDao.getMediaDataByUri(mediaItem.getUri());

          if(dbMediaData == null) {
            MediaId mediaId = MediaDataDao.createMediaId(mediaItem.getUri());

            dbMediaData = mediaDataDao.getMediaDataByHash(mediaId.getHash());

            if(dbMediaData == null) {
              dbMediaData = new hs.mediasystem.dao.MediaData();
            }

            dbMediaData.setUri(mediaItem.getUri());  // replace uri, as it didn't match anymore
            dbMediaData.setMediaId(mediaId);  // replace mediaId, even though hash matches, to update the other values just in case
            dbMediaData.setLastUpdated(new Date());

            mediaDataDao.storeMediaData(dbMediaData);
          }

          return entityFactory.create(MediaData.class, dbMediaData);
        }
      })
      .finish(new FinishEnrichCallback<MediaData>() {
        @Override
        public void update(MediaData result) {
          mediaItem.mediaData.set(result);
        }
      })
      .build()
    );

    mediaItem.identifier.setEnricher(new EnricherBuilder<MediaItem, Identifier>(Identifier.class)
      .require(mediaItem.mediaData)
      .enrich(new EnrichCallback<Identifier>() {
        @Override
        public Identifier enrich(Object... parameters) {
          MediaData mediaData = (MediaData)parameters[0];

          hs.mediasystem.dao.MediaData associatedKey = entityFactory.getAssociatedKey(mediaData);
          hs.mediasystem.dao.Identifier dbIdentifier = identifierDao.getIdentifierByMediaDataId(associatedKey.getId());

          /*
           * It's possible this Identifier is an empty placeholder to prevent attempts at identifying a Media every time it
           * is accessed.  This is negative caching a failed identification.  However, if this was more than a week ago the
           * identification should be re-attempted.  To trigger this, null is returned.
           */

          if(dbIdentifier != null && dbIdentifier.getProviderId() == null && dbIdentifier.getLastUpdated().getTime() < System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000) {
            return null;
          }

          return dbIdentifier == null ? null : entityFactory.create(Identifier.class, dbIdentifier);
        }
      })
      .enrich(new EnrichCallback<Identifier>() {
        @Override
        public Identifier enrich(Object... parameters) {
          MediaData mediaData = (MediaData)parameters[0];

          hs.mediasystem.dao.Identifier dbIdentifier = null;

          try {
            dbIdentifier = mediaIdentifier.identifyItem(mediaItem);
          }
          catch(IdentifyException e) {
            dbIdentifier = new hs.mediasystem.dao.Identifier();
          }

          hs.mediasystem.dao.MediaData associatedKey = entityFactory.getAssociatedKey(mediaData);

          dbIdentifier.setMediaData(associatedKey);
          dbIdentifier.setLastUpdated(new Date());

          hs.mediasystem.dao.Identifier existingIdentifier = identifierDao.getIdentifierByMediaDataId(associatedKey.getId());

          if(existingIdentifier != null) {
            dbIdentifier.setId(existingIdentifier.getId());
          }

          identifierDao.storeIdentifier(dbIdentifier);

          return entityFactory.create(Identifier.class, dbIdentifier);
        }
      })
      .finish(new FinishEnrichCallback<Identifier>() {
        @Override
        public void update(Identifier result) {
          mediaItem.identifier.set(result);
        }
      })
      .build()
    );
  }
}
