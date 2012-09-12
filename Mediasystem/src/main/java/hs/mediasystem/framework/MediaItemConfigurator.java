package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.dao.MediaId;
import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.FinishEnrichCallback;

import java.util.Date;

import javax.inject.Inject;

public class MediaItemConfigurator {
  private final IdentifierDao identifierDao;
  private final MediaDataDao mediaDataDao;

  @Inject
  public MediaItemConfigurator(IdentifierDao identifierDao, MediaDataDao mediaDataDao) {
    this.identifierDao = identifierDao;
    this.mediaDataDao = mediaDataDao;
  }

  public void configure(final MediaItem mediaItem, final MediaIdentifier mediaIdentifier) {
    mediaItem.mediaData.setEnricher(new EnricherBuilder<MediaItem, MediaData>(MediaData.class)
      .enrich(new EnrichCallback<MediaData>() {
        @Override
        public MediaData enrich(Object... parameters) {
          MediaData mediaData = mediaDataDao.getMediaDataByUri(mediaItem.getUri());

          if(mediaData == null) {
            MediaId mediaId = MediaDataDao.createMediaId(mediaItem.getUri());

            mediaData = mediaDataDao.getMediaDataByHash(mediaId.getHash());

            if(mediaData == null) {
              mediaData = new MediaData();
            }

            mediaData.setUri(mediaItem.getUri());  // replace uri, as it didn't match anymore
            mediaData.setMediaId(mediaId);  // replace mediaId, even though hash matches, to update the other values just in case
            mediaData.setLastUpdated(new Date());

            mediaDataDao.storeMediaData(mediaData);
          }

          return mediaData;
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

          Identifier identifier = identifierDao.getIdentifierByMediaDataId(mediaData.getId());

          /*
           * It's possible this Identifier is an empty placeholder to prevent attempts at identifying a Media every time it
           * is accessed.  This is negative caching a failed identification.  However, if this was more than a week ago the
           * identification should be re-attempted.  To trigger this, null is returned.
           */

          if(identifier != null && identifier.getProviderId() == null && identifier.getLastUpdated().getTime() < System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000) {
            return null;
          }

          return identifier;
        }
      })
      .enrich(new EnrichCallback<Identifier>() {
        @Override
        public Identifier enrich(Object... parameters) {
          MediaData mediaData = (MediaData)parameters[0];

          Identifier identifier = null;

          try {
            identifier = mediaIdentifier.identifyItem(mediaItem.getMedia());
          }
          catch(IdentifyException e) {
            identifier = new Identifier();
          }

          identifier.setMediaData(mediaData);
          identifier.setLastUpdated(new Date());

          Identifier existingIdentifier = identifierDao.getIdentifierByMediaDataId(mediaData.getId());

          if(existingIdentifier != null) {
            identifier.setId(existingIdentifier.getId());
          }

          identifierDao.storeIdentifier(identifier);

          return identifier;
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
