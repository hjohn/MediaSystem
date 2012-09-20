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
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemConfigurator;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.persist.PersistQueue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SeriesMediaTree implements MediaTree, MediaRoot {
  private final PersistQueue persister;
  private final ItemsDao itemsDao;
  private final MediaItemConfigurator mediaItemConfigurator;
  private final EntityFactory<DatabaseObject> entityFactory;
  private final List<Path> roots;

  private List<MediaItem> children;

  public SeriesMediaTree(PersistQueue persister, ItemsDao itemsDao, MediaItemConfigurator mediaItemConfigurator, EntityFactory<DatabaseObject> entityFactory, List<Path> roots) {
    this.persister = persister;
    this.itemsDao = itemsDao;
    this.mediaItemConfigurator = mediaItemConfigurator;
    this.entityFactory = entityFactory;
    this.roots = new ArrayList<>(roots);
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Path root : roots) {
        List<LocalInfo> scanResults = new SerieScanner().scan(root);

        for(LocalInfo localInfo : scanResults) {
          final SerieItem mediaItem = new SerieItem(SeriesMediaTree.this, localInfo.getUri(), localInfo.getTitle(), entityFactory, mediaItemConfigurator, itemsDao);

          mediaItemConfigurator.configure(mediaItem, Activator.TVDB_SERIE_ENRICHER);

          mediaItem.media.setEnricher(new EnricherBuilder<MediaItem, Serie>(Serie.class)
            .require(mediaItem.identifier)
            .enrich(new EnrichCallback<Serie>() {
              @Override
              public Serie enrich(Object... parameters) {
                Identifier identifier = ((hs.mediasystem.framework.Identifier)parameters[0]).getKey();

                if(identifier.getProviderId() != null) {
                  try {
                    return (Serie)entityFactory.create(Media.class, itemsDao.loadItem(identifier.getProviderId()));
                  }
                  catch(ItemNotFoundException e) {
                    return null;
                  }
                }

                return null;
              }
            })
            .enrich(new EnrichCallback<Serie>() {
              @Override
              public Serie enrich(Object... parameters) {
                Identifier identifier = ((hs.mediasystem.framework.Identifier)parameters[0]).getKey();

                if(identifier.getProviderId() != null) {
                  try {
                    Item item = Activator.TVDB_SERIE_ENRICHER.loadItem(identifier.getProviderId());

                    if(item != null) {
                      itemsDao.storeItem(item);  // FIXME should also update if version or bypasscache problem
                    }

                    return (Serie)entityFactory.create(Media.class, item);
                  }
                  catch(ItemNotFoundException e) {
                    System.out.println("[FINE] Item not found (in Database or TVDB): " + identifier.getProviderId());
                  }
                }

                return null;
              }
            })
            .finish(new FinishEnrichCallback<Serie>() {
              @Override
              public void update(Serie result) {
                mediaItem.media.set(result);
              }
            })
            .build()
          );

          children.add(mediaItem);
        }
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "Series";
  }

  @Override
  public PersistQueue getPersister() {
    return persister;
  }

  @Override
  public String getId() {
    return "serieRoot";
  }

  @Override
  public MediaRoot getParent() {
    return null;
  }
}
