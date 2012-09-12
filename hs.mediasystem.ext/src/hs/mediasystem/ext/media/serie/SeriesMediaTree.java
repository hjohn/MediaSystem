package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.EntityFactory;
import hs.mediasystem.entity.FinishEnrichCallback;
import hs.mediasystem.entity.InstanceEnricher;
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
  private final EntityFactory entityFactory;
  private final List<Path> roots;

  private List<MediaItem> children;

  public SeriesMediaTree(PersistQueue persister, ItemsDao itemsDao, MediaItemConfigurator mediaItemConfigurator, EntityFactory entityFactory, List<Path> roots) {
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
          final Serie serie = new Serie(localInfo.getTitle());

          serie.setEntityFactory(entityFactory);

          SerieItem mediaItem = new SerieItem(SeriesMediaTree.this, localInfo.getUri(), serie, entityFactory, mediaItemConfigurator, itemsDao);

          mediaItemConfigurator.configure(mediaItem, Activator.TVDB_SERIE_ENRICHER);

          InstanceEnricher<Serie, Void> enricher = new EnricherBuilder<Serie, Item>(Item.class)
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
                    Item item = Activator.TVDB_SERIE_ENRICHER.loadItem(identifier.getProviderId());

                    if(item != null) {
                      itemsDao.storeItem(item);  // FIXME should also update if version or bypasscache problem
                    }

                    return item;
                  }
                  catch(ItemNotFoundException e) {
                    System.out.println("[FINE] Item not found (in Database or TVDB): " + identifier.getProviderId());
                  }
                }

                return null;
              }
            })
            .finish(new FinishEnrichCallback<Item>() {
              @Override
              public void update(Item result) {
                serie.item.set(result);
              }
            })
            .build();

          serie.setEnricher(enricher);

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
