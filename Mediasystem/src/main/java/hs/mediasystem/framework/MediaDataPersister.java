package hs.mediasystem.framework;

import java.util.Date;

import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.entity.EntityPersister;
import hs.mediasystem.persist.Persister;

import javax.inject.Inject;

@EntityPersister(entityClass = MediaData.class, sourceClass = DatabaseEntitySource.class)
public class MediaDataPersister implements Persister<MediaData, Integer> {
  private final MediaDataDao mediaDataDao;

  @Inject
  public MediaDataPersister(MediaDataDao mediaDataDao) {
    this.mediaDataDao = mediaDataDao;
  }

  // TODO this whole concept is not thread-safe... MediaData might get modified while reading values here
  @Override
  public void persist(MediaData mediaData, Integer key) {
    hs.mediasystem.dao.MediaData dbMediaData = mediaDataDao.getMediaData(key);

    dbMediaData.setViewed(mediaData.viewed.get());
    dbMediaData.setResumePosition(mediaData.resumePosition.get());

    dbMediaData.getIdentifiers().clear();

    for(Identifier identifier : mediaData.identifiers.get()) {
      // TODO this is a mix of Entity level and Database level values
      hs.mediasystem.dao.Identifier dbIdentifier = new hs.mediasystem.dao.Identifier(identifier.providerId.get(), identifier.matchType.get(), identifier.matchAccuracy.get());

      dbIdentifier.setMediaData(dbMediaData);
      dbIdentifier.setLastUpdated(new Date());

      dbMediaData.getIdentifiers().add(dbIdentifier);
    }

    mediaDataDao.updateMediaData(dbMediaData);
  }
}
