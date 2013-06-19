package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.EntityFactory;
import hs.mediasystem.entity.FinishEnrichCallback;
import hs.mediasystem.framework.EpisodeScanner;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemConfigurator;
import hs.mediasystem.framework.MediaRoot;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SerieItem extends MediaItem implements MediaRoot {
  private static final TvdbEpisodeEnricher TVDB_EPISODE_ENRICHER = new TvdbEpisodeEnricher(SeriesMediaTree.TVDB_SERIE_ENRICHER);

  private final SeriesMediaTree mediaRoot;
  private final EntityFactory<DatabaseObject> entityFactory;
  private final MediaItemConfigurator mediaItemConfigurator;
  private final ItemsDao itemsDao;
  private final Id id;

  private List<MediaItem> children;

  public SerieItem(SeriesMediaTree mediaTree, String uri, String serieTitle, EntityFactory<DatabaseObject> entityFactory, MediaItemConfigurator mediaItemConfigurator, ItemsDao itemsDao) {
    super(uri, serieTitle, Serie.class);

    this.id = new Id("serie");
    this.mediaRoot = mediaTree;
    this.entityFactory = entityFactory;
    this.mediaItemConfigurator = mediaItemConfigurator;
    this.itemsDao = itemsDao;
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new EpisodeScanner(new EpisodeDecoder(getTitle()), 2).scan(Paths.get(getUri()));

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        final MediaItem mediaItem = new MediaItem(localInfo.getUri(), getMedia().title.get(), null, localInfo.getSeason() + "x" + localInfo.getEpisode(), null, Episode.class);

        mediaItem.properties.put("serie", this);
        mediaItem.properties.put("season", localInfo.getSeason());
        mediaItem.properties.put("episodeNumber", localInfo.getEpisode());
        mediaItem.properties.put("endEpisode", localInfo.getEndEpisode());
        mediaItem.properties.put("episodeRange", localInfo.getEpisode() == null ? null : ("" + localInfo.getEpisode() + (localInfo.getEndEpisode() != localInfo.getEpisode() ? "-" + localInfo.getEndEpisode() : "")));

        mediaItemConfigurator.configure(mediaItem, TVDB_EPISODE_ENRICHER);

        mediaItem.media.setEnricher(new EnricherBuilder<MediaItem, Episode>(Episode.class)
          .require(mediaItem.identifier)
          .enrich(new EnrichCallback<Episode>() {
            @Override
            public Episode enrich(Object... parameters) {
              Identifier identifier = ((hs.mediasystem.framework.Identifier)parameters[0]).getKey();

              if(identifier.getProviderId() != null) {
                try {
                  // FIXME should check version after loading, and bypasscache
                  return (Episode)entityFactory.create(Media.class, itemsDao.loadItem(identifier.getProviderId()));
                }
                catch(ItemNotFoundException e) {
                  return null;
                }
              }

              return null;
            }
          })
          .enrich(new EnrichCallback<Episode>() {
            @Override
            public Episode enrich(Object... parameters) {
              Identifier identifier = ((hs.mediasystem.framework.Identifier)parameters[0]).getKey();

              if(identifier.getProviderId() != null) {
                try {
                  Item item = TVDB_EPISODE_ENRICHER.loadItem(identifier.getProviderId());

                  if(item != null) {
                    itemsDao.storeItem(item);  // FIXME should also update if version or bypasscache problem
                  }

                  return (Episode)entityFactory.create(Media.class, item);
                }
                catch(ItemNotFoundException e) {
                  System.out.println("[FINE] Item not found (in Database or TVDB): " + identifier.getProviderId());
                }
              }

              return null;
            }
          })
          .finish(new FinishEnrichCallback<Episode>() {
            @Override
            public void update(Episode result) {
              mediaItem.media.set(result);
            }
          })
          .build()
        );

        children.add(mediaItem);
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return getTitle();
  }

  @Override
  public Id getId() {
    return id;
  }

  @Override
  public MediaRoot getParent() {
    return mediaRoot;
  }

  private static final Map<String, Object> MEDIA_PROPERTIES = new HashMap<>();

  static {
    MEDIA_PROPERTIES.put("image.poster", null);
    MEDIA_PROPERTIES.put("image.poster.aspectRatios", new double[] {16.0 / 9.0, 4.0 / 3.0});
    MEDIA_PROPERTIES.put("image.poster.hasIdentifyingTitle", false);

    MEDIA_PROPERTIES.put("image.background", null);
    MEDIA_PROPERTIES.put("image.background.aspectRatios", new double[] {2.0 / 3.0, 4.0 / 3.0});
    MEDIA_PROPERTIES.put("image.background.hasIdentifyingTitle", false);
    MEDIA_PROPERTIES.put("image.background.fromParent", true);

    MEDIA_PROPERTIES.put("image.banner", null);
    MEDIA_PROPERTIES.put("image.banner.aspectRatios", new double[] {6.0 / 1.0});
    MEDIA_PROPERTIES.put("image.banner.hasIdentifyingTitle", true);
    MEDIA_PROPERTIES.put("image.banner.fromParent", true);
  }

  @Override
  public Map<String, Object> getMediaProperties() {
    return Collections.unmodifiableMap(MEDIA_PROPERTIES);
  }
}
