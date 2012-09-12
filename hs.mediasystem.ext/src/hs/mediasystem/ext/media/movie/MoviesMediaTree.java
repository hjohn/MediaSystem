package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.dao.MediaId;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.enrich.InstanceEnricher;
import hs.mediasystem.framework.EnrichCallback;
import hs.mediasystem.framework.EnricherBuilder;
import hs.mediasystem.framework.EntityFactory;
import hs.mediasystem.framework.FinishEnrichCallback;
import hs.mediasystem.framework.IdentifyException;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.EpisodeScanner;
import hs.mediasystem.persist.PersistQueue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MoviesMediaTree implements MediaTree, MediaRoot {
  private final EnrichCache enrichCache;
  private final PersistQueue persister;
  private final List<Path> roots;
  private final ItemsDao itemsDao;
  private final MediaDataDao mediaDataDao;
  private final IdentifierDao identifierDao;
  private final EntityFactory entityFactory;

  private List<MediaItem> children;

  public MoviesMediaTree(EnrichCache enrichCache, PersistQueue persister, ItemsDao itemsDao, MediaDataDao mediaDataDao, IdentifierDao identifierDao, EntityFactory entityFactory, List<Path> roots) {
    this.enrichCache = enrichCache;
    this.persister = persister;
    this.itemsDao = itemsDao;
    this.mediaDataDao = mediaDataDao;
    this.identifierDao = identifierDao;
    this.entityFactory = entityFactory;
    this.roots = new ArrayList<>(roots);
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Path root : roots) {
        List<LocalInfo> scanResults = new EpisodeScanner(new MovieDecoder(), 1).scan(root);

        for(final LocalInfo localInfo : scanResults) {
          final Movie movie = new Movie(localInfo.getTitle(), localInfo.getEpisode(), localInfo.getSubtitle(), localInfo.getReleaseYear(), localInfo.getCode());

          movie.setEntityFactory(entityFactory);

          final MediaItem mediaItem = new MediaItem(MoviesMediaTree.this, localInfo.getUri(), movie);

          InstanceEnricher<Movie, Void> enricher = new EnricherBuilder<Movie, Item>(Item.class)
            .require(mediaItem.identifier)
            .enrich(new EnrichCallback<Item>() {
              @Override
              public Item enrich(Object... parameters) {
                Identifier identifier = (Identifier)parameters[0];

                if(identifier.getProviderId() != null) {
                  try {
                    return itemsDao.loadItem(identifier.getProviderId());
                  }
                  catch(ItemNotFoundException e) {
                    return null;
                  }
                }

                return null;
              }
            })
            .enrich(new EnrichCallback<Item>() {
              @Override
              public Item enrich(Object... parameters) {
                Identifier identifier = (Identifier)parameters[0];

                if(identifier.getProviderId() != null) {
                  try {
                    Item item = Activator.TMDB_ENRICHER.loadItem(identifier.getProviderId());

                    if(item != null) {
                      itemsDao.storeItem(item);  // FIXME should also update if version or bypasscache problem
                    }

                    return item;
                  }
                  catch(ItemNotFoundException e) {
                    System.out.println("[FINE] Item not found (in Database or TMDB): " + identifier.getProviderId());
                  }
                }

                return null;
              }
            })
            .finish(new FinishEnrichCallback<Item>() {
              @Override
              public void update(Item result) {
                movie.item.set(result);
              }
            })
            .build();

          mediaItem.mediaData.setEnricher(new EnricherBuilder<MediaItem, MediaData>(MediaData.class)
            .enrich(new EnrichCallback<MediaData>() {
              @Override
              public MediaData enrich(Object... parameters) {
                MediaData mediaData = mediaDataDao.getMediaDataByUri(localInfo.getUri());

                if(mediaData == null) {
                  MediaId mediaId = MediaDataDao.createMediaId(localInfo.getUri());

                  mediaData = mediaDataDao.getMediaDataByHash(mediaId.getHash());

                  if(mediaData == null) {
                    mediaData = new MediaData();
                  }

                  mediaData.setUri(localInfo.getUri());  // replace uri, as it didn't match anymore
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
                  identifier = Activator.TMDB_ENRICHER.identifyItem(movie);
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

          movie.setEnricher(enricher);

          children.add(mediaItem);
        }
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "Movies";
  }

  @Override
  public EnrichCache getEnrichCache() {
    return enrichCache;
  }

  @Override
  public PersistQueue getPersister() {
    return persister;
  }

  @Override
  public String getId() {
    return "movieRoot";
  }

  @Override
  public MediaRoot getParent() {
    return null;
  }
}
