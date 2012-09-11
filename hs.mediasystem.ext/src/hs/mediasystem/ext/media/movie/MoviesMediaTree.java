package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.enrich.InstanceEnricher;
import hs.mediasystem.framework.EnrichCallback;
import hs.mediasystem.framework.EnricherBuilder;
import hs.mediasystem.framework.EntityFactory;
import hs.mediasystem.framework.FinishEnrichCallback;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.EpisodeScanner;
import hs.mediasystem.persist.PersistQueue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MoviesMediaTree implements MediaTree, MediaRoot {
  private final EnrichCache enrichCache;
  private final PersistQueue persister;
  private final List<Path> roots;
  private final ItemsDao itemsDao;
  private final EntityFactory entityFactory;

  private List<MediaItem> children;

  public MoviesMediaTree(EnrichCache enrichCache, PersistQueue persister, ItemsDao itemsDao, EntityFactory entityFactory, List<Path> roots) {
    this.enrichCache = enrichCache;
    this.persister = persister;
    this.itemsDao = itemsDao;
    this.entityFactory = entityFactory;
    this.roots = new ArrayList<>(roots);
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Path root : roots) {
        List<LocalInfo> scanResults = new EpisodeScanner(new MovieDecoder(), 1).scan(root);

        for(LocalInfo localInfo : scanResults) {
          final Movie movie = new Movie(localInfo.getTitle(), localInfo.getEpisode(), localInfo.getSubtitle(), localInfo.getReleaseYear(), localInfo.getCode());

          movie.setEntityFactory(entityFactory);

          final MediaItem mediaItem = new MediaItem(MoviesMediaTree.this, localInfo.getUri(), movie);

          InstanceEnricher<Movie, Void> enricher = new EnricherBuilder<Movie, Item>()
            .require(mediaItem.dataMapProperty().get(), Identifier.class)
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
