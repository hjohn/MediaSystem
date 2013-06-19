package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.EntityFactory;
import hs.mediasystem.entity.FinishEnrichCallback;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemConfigurator;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.ScanException;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.util.PathStringConverter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;

import javax.inject.Inject;

public class SeriesMediaTree implements MediaTree, MediaRoot {
  public static final TvdbSerieEnricher TVDB_SERIE_ENRICHER = new TvdbSerieEnricher();

  private static final Id ID = new Id("serieRoot");

  private final PersistQueue persister;
  private final ItemsDao itemsDao;
  private final MediaItemConfigurator mediaItemConfigurator;
  private final EntityFactory<DatabaseObject> entityFactory;
  private final List<Path> roots;

  private List<MediaItem> children;

  @Inject
  public SeriesMediaTree(PersistQueue persister, ItemsDao itemsDao, MediaItemConfigurator mediaItemConfigurator, EntityFactory<DatabaseObject> entityFactory, SettingsStore settingsStore) {
    this.persister = persister;
    this.itemsDao = itemsDao;
    this.mediaItemConfigurator = mediaItemConfigurator;
    this.entityFactory = entityFactory;

    ObservableList<Path> paths = settingsStore.getListProperty("MediaSystem:Ext:Series", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

    this.roots = new ArrayList<>(paths);
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Path root : roots) {
        try {
          List<LocalInfo> scanResults = new SerieScanner().scan(root);

          for(LocalInfo localInfo : scanResults) {
            final SerieItem mediaItem = new SerieItem(SeriesMediaTree.this, localInfo.getUri(), localInfo.getTitle(), entityFactory, mediaItemConfigurator, itemsDao);

            mediaItemConfigurator.configure(mediaItem, TVDB_SERIE_ENRICHER);

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
                      Item item = TVDB_SERIE_ENRICHER.loadItem(identifier.getProviderId());

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
        catch(ScanException e) {
          System.err.println("[WARN] SeriesMediaTree: " + e.getMessage());  // TODO add to some high level user error reporting facility
          e.printStackTrace();
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
  public Id getId() {
    return ID;
  }

  @Override
  public MediaRoot getParent() {
    return null;
  }

  private static final Map<String, Object> MEDIA_PROPERTIES = new HashMap<>();

  static {
    MEDIA_PROPERTIES.put("image.poster", null);
    MEDIA_PROPERTIES.put("image.poster.aspectRatios", new double[] {2.0 / 3.0});
    MEDIA_PROPERTIES.put("image.poster.hasIdentifyingTitle", true);

    MEDIA_PROPERTIES.put("image.background", null);
    MEDIA_PROPERTIES.put("image.background.aspectRatios", new double[] {16.0 / 9.0, 4.0 / 3.0});
    MEDIA_PROPERTIES.put("image.background.hasIdentifyingTitle", false);

    MEDIA_PROPERTIES.put("image.banner", null);
    MEDIA_PROPERTIES.put("image.banner.aspectRatios", new double[] {6.0 / 1.0});
    MEDIA_PROPERTIES.put("image.banner.hasIdentifyingTitle", true);
  }

  @Override
  public Map<String, Object> getMediaProperties() {
    return Collections.unmodifiableMap(MEDIA_PROPERTIES);
  }
}
