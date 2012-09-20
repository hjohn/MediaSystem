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
import hs.mediasystem.entity.InstanceEnricher;
import hs.mediasystem.framework.EpisodeScanner;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemConfigurator;
import hs.mediasystem.framework.MediaRoot;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SerieItem extends MediaItem implements MediaRoot {
  private final SeriesMediaTree mediaRoot;
  private final EntityFactory<DatabaseObject> entityFactory;
  private final MediaItemConfigurator mediaItemConfigurator;
  private final ItemsDao itemsDao;

  private List<MediaItem> children;

  public SerieItem(SeriesMediaTree mediaTree, String uri, String serieTitle, EntityFactory<DatabaseObject> entityFactory, MediaItemConfigurator mediaItemConfigurator, ItemsDao itemsDao) {
    super(uri, serieTitle, Serie.class);

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
//        final Episode episode = new Episode((Serie)this.getMedia(), getTitle() + " " + localInfo.getSeason() + "x" + localInfo.getEpisode(), localInfo.getSeason(), localInfo.getEpisode(), localInfo.getEndEpisode());
//
//        episode.setEntityFactory(entityFactory);

        final MediaItem mediaItem = new MediaItem(localInfo.getUri(), getMedia().title.get(), null, localInfo.getSeason() + "x" + localInfo.getEpisode(), null, Episode.class);

        mediaItem.properties.put("serie", this);
        mediaItem.properties.put("season", localInfo.getSeason());
        mediaItem.properties.put("episodeNumber", localInfo.getEpisode());
        mediaItem.properties.put("endEpisode", localInfo.getEndEpisode());
        mediaItem.properties.put("episodeRange", localInfo.getEpisode() == null ? null : ("" + localInfo.getEpisode() + (localInfo.getEndEpisode() != localInfo.getEpisode() ? "-" + localInfo.getEndEpisode() : "")));

        mediaItemConfigurator.configure(mediaItem, Activator.TVDB_EPISODE_ENRICHER);

        InstanceEnricher<MediaItem, Void> enricher = new EnricherBuilder<MediaItem, Episode>(Episode.class)
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
                    Item item = Activator.TVDB_EPISODE_ENRICHER.loadItem(identifier.getProviderId());

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
            .build();

        mediaItem.media.setEnricher(enricher);

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
  public String getId() {
    return "serie[" + getTitle() + "]";
  }

  @Override
  public MediaRoot getParent() {
    return mediaRoot;
  }

}
