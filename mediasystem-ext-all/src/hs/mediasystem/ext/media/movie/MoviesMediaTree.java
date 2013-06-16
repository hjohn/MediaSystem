package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.entity.EnrichCallback;
import hs.mediasystem.entity.EnricherBuilder;
import hs.mediasystem.entity.EntityFactory;
import hs.mediasystem.entity.FinishEnrichCallback;
import hs.mediasystem.framework.EpisodeScanner;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemConfigurator;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.util.PathStringConverter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import javafx.collections.ObservableList;

public class MoviesMediaTree implements MediaTree, MediaRoot {
  private static final TmdbMovieEnricher TMDB_ENRICHER = new TmdbMovieEnricher();

  private final PersistQueue persister;
  private final List<Path> roots;
  private final ItemsDao itemsDao;
  private final MediaItemConfigurator mediaItemConfigurator;
  private final EntityFactory<DatabaseObject> entityFactory;

  private List<MediaItem> children;

  @Inject
  public MoviesMediaTree(PersistQueue persister, ItemsDao itemsDao, MediaItemConfigurator mediaItemConfigurator, EntityFactory<DatabaseObject> entityFactory, SettingsStore settingsStore) {
    this.persister = persister;
    this.itemsDao = itemsDao;
    this.mediaItemConfigurator = mediaItemConfigurator;
    this.entityFactory = entityFactory;

    ObservableList<Path> paths = settingsStore.getListProperty("MediaSystem:Ext:Movies", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

    this.roots = new ArrayList<>(paths);
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Path root : roots) {
        List<LocalInfo> scanResults = new EpisodeScanner(new MovieDecoder(), 1).scan(root);
        List<Object[]> fullItems = itemsDao.loadFullItems(root.toString());

        for(final LocalInfo localInfo : scanResults) {
          final MediaItem mediaItem = new MediaItem(localInfo.getUri(), null, localInfo.getTitle(), localInfo.getEpisode() == null ? null : "" + localInfo.getEpisode(), localInfo.getSubtitle(), Movie.class);

          mediaItem.properties.put("releaseYear", localInfo.getReleaseYear());
          mediaItem.properties.put("imdbNumber", localInfo.getCode());

          for(Object[] objects : fullItems) {
            MediaData mediaData = (MediaData)objects[0];

            if(mediaData.getUri().equals(localInfo.getUri())) {
              mediaItem.mediaData.set(entityFactory.create(hs.mediasystem.framework.MediaData.class, mediaData));

              if(objects[1] != null) {
                mediaItem.identifier.set(entityFactory.create(hs.mediasystem.framework.Identifier.class, (Identifier)objects[1]));

                if(objects[2] != null) {
                  mediaItem.media.set(entityFactory.create(Media.class, (Item)objects[2]));
                }
              }

              break;
            }
          }

          mediaItemConfigurator.configure(mediaItem, TMDB_ENRICHER);

          mediaItem.media.setEnricher(new EnricherBuilder<MediaItem, Movie>(Movie.class)
            .require(mediaItem.identifier)
            .enrich(new EnrichCallback<Movie>() {
              @Override
              public Movie enrich(Object... parameters) {
                Identifier identifier = ((hs.mediasystem.framework.Identifier)parameters[0]).getKey();

                if(identifier.getProviderId() != null) {
                  try {
                    return (Movie)entityFactory.create(Media.class, itemsDao.loadItem(identifier.getProviderId()));
                  }
                  catch(ItemNotFoundException e) {
                    return null;
                  }
                }

                return null;
              }
            })
            .enrich(new EnrichCallback<Movie>() {
              @Override
              public Movie enrich(Object... parameters) {
                Identifier identifier = ((hs.mediasystem.framework.Identifier)parameters[0]).getKey();

                if(identifier.getProviderId() != null) {
                  try {
                    Item item = TMDB_ENRICHER.loadItem(identifier.getProviderId());

                    if(item != null) {
                      itemsDao.storeItem(item);  // FIXME should also update if version or bypasscache problem
                    }

                    return (Movie)entityFactory.create(Media.class, item);
                  }
                  catch(ItemNotFoundException e) {
                    System.out.println("[FINE] Item not found (in Database or TMDB): " + identifier.getProviderId());
                  }
                }

                return null;
              }
            })
            .finish(new FinishEnrichCallback<Movie>() {
              @Override
              public void update(Movie result) {
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
    return "Movies";
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

  private static final Map<String, Object> MEDIA_PROPERTIES = new HashMap<>();

  static {
    MEDIA_PROPERTIES.put("image.poster", null);
    MEDIA_PROPERTIES.put("image.poster.aspectRatios", new double[] {2.0 / 3.0});
    MEDIA_PROPERTIES.put("image.poster.hasIdentifyingTitle", true);

    MEDIA_PROPERTIES.put("image.background", null);
    MEDIA_PROPERTIES.put("image.background.aspectRatios", new double[] {16.0 / 9.0, 4.0 / 3.0});
    MEDIA_PROPERTIES.put("image.background.hasIdentifyingTitle", false);
  }

  @Override
  public Map<String, Object> getMediaProperties() {
    return Collections.unmodifiableMap(MEDIA_PROPERTIES);
  }
}
