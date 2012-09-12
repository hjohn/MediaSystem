package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.MediaDataDao;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.EntityFactory;
import hs.mediasystem.framework.EntityProvider;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.SubtitleCriteriaProvider;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.AbstractSetting;
import hs.mediasystem.screens.DefaultMediaGroup;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.screens.MediaNodeCellProvider;
import hs.mediasystem.screens.Setting;
import hs.mediasystem.screens.SettingGroup;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.PathListOption;
import hs.mediasystem.util.PathStringConverter;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javafx.collections.ObservableList;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {
  static final TmdbMovieEnricher TMDB_ENRICHER = new TmdbMovieEnricher();

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(Setting.class.getName(), null)
      .setImplementation(new SettingGroup(context, "movies", "Movies", 0))
    );

    manager.add(createComponent()
      .setInterface(Setting.class.getName(), new Hashtable<String, Object>() {{
        put("parentId", "movies");
      }})
      .setImplementation(new AbstractSetting("movies.add-remove", 0) {
        private volatile SettingsStore settingsStore;

        @Override
        public Option createOption() {
          final ObservableList<Path> moviePaths = settingsStore.getListProperty("MediaSystem:Ext:Movies", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

          return new PathListOption("Add/Remove Movie folder", moviePaths);
        }
      })
      .add(createServiceDependency()
        .setService(SettingsStore.class)
        .setRequired(true)
      )
    );

    manager.add(createComponent()
      .setInterface(MediaNodeCellProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Movie.class);
        put("type", MediaNodeCellProvider.Type.HORIZONTAL);
      }})
      .setImplementation(new MediaNodeCellProvider() {
        @Override
        public MediaNodeCell get() {
          return new MovieCell();
        }
      })
    );

    manager.add(createComponent()
      .setInterface(SubtitleCriteriaProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Movie.class);
      }})
      .setImplementation(new SubtitleCriteriaProvider() {
        @Override
        public Map<String, Object> getCriteria(MediaItem mediaItem) {
          Media<?> media = mediaItem.getMedia();
          MediaData mediaData = mediaItem.get(MediaData.class);

          Map<String, Object> criteria = new HashMap<>();

          criteria.put(SubtitleCriteriaProvider.TITLE, media.title.get());

          if(media instanceof Movie) {
            Movie movie = (Movie)media;

            criteria.put(SubtitleCriteriaProvider.YEAR, movie.releaseYear.get());
            criteria.put(SubtitleCriteriaProvider.IMDB_ID, movie.imdbNumber.get());
          }

          if(mediaData != null) {
            criteria.put(SubtitleCriteriaProvider.OPEN_SUBTITLES_HASH, mediaData.getMediaId().getOsHash());
            criteria.put(SubtitleCriteriaProvider.FILE_LENGTH, mediaData.getMediaId().getFileLength());
          }

          return criteria;
        }
      })
    );

    manager.add(createComponent()
      .setInterface(MediaGroup.class.getName(), new Hashtable<String, Object>() {{
        put(MediaGroup.Constants.MEDIA_ROOT_CLASS.name(), MoviesMediaTree.class);
      }})
      .setImplementation(new DefaultMediaGroup("alpha-group-title", "Alphabetically, grouped by Title", new MovieGrouper(), MovieTitleGroupingComparator.INSTANCE, false, false) {
        @Override
        public Media<?> createMediaFromFirstItem(MediaItem item) {
          return new Media<>(item.getTitle(), null, item.getMedia().releaseYear.get());
        }
      })
    );

    manager.add(createComponent()
      .setInterface(MediaGroup.class.getName(), new Hashtable<String, Object>() {{
        put(MediaGroup.Constants.MEDIA_ROOT_CLASS.name(), MoviesMediaTree.class);
      }})
      .setImplementation(new DefaultMediaGroup("alpha", "Alphabetically", null, MovieTitleGroupingComparator.INSTANCE, false, false))
    );

    manager.add(createComponent()
      .setInterface(EntityProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Media.class);
      }})
      .setImplementation(new EntityProvider<Movie>() {
        @Override
        public Movie get(Object... parameters) {
          if(parameters.length != 1 || !(parameters[0] instanceof Item) || !((Item)parameters[0]).getProviderId().getType().equals("Movie")) {
            return null;
          }

          Item item = (Item)parameters[0];

          Movie movie = new Movie(item.getTitle(), item.getEpisode(), "", null, item.getImdbId());

          movie.item.set(item);

          return movie;
        }
      })
    );

    manager.add(createComponent()
      .setInterface(MainMenuExtension.class.getName(), null)
      .setImplementation(MoviesMainMenuExtension.class)
      .add(createServiceDependency()
        .setService(EnrichCache.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(PersistQueue.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(MediaDataDao.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(ItemsDao.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(EntityFactory.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(IdentifierDao.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(SettingsStore.class)
        .setRequired(true)
      )
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }
}
