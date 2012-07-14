package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.IdentifierDao;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleCriteriaProvider;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.ConfigurationOption;
import hs.mediasystem.screens.DefaultMediaGroup;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.screens.MediaNodeCellProvider;
import hs.mediasystem.screens.optiondialog.PathSelectOption;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.SubOption;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;
import hs.mediasystem.util.Callable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javafx.beans.property.SimpleObjectProperty;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(ConfigurationOption.class.getName(), null)
      .setImplementation(new ConfigurationOption() {
        @Override
        public double order() {
          return 0;
        }

        @Override
        public String getTitle() {
          return "Movies";
        }

        @Override
        public String getParentId() {
          return null;
        }

        @Override
        public String getId() {
          return "movies";
        }

        @Override
        public Option createOption() {
          return null;
        }
      })
    );

    manager.add(createComponent()
      .setInterface(ConfigurationOption.class.getName(), null)
      .setImplementation(new ConfigurationOption() {
        @Override
        public double order() {
          return 0;
        }

        @Override
        public String getTitle() {
          return "Add/Remove folder";
        }

        @Override
        public String getParentId() {
          return "movies";
        }

        @Override
        public String getId() {
          return "movies";
        }

        @Override
        public Option createOption() {
          return new SubOption(getTitle(), new Callable<List<Option>>() {
            @Override
            public List<Option> call() {
              List<Option> options = new ArrayList<>();

              options.add(new SubOption("Add folder", new Callable<List<Option>>() {
                @Override
                public List<Option> call() {
                  List<Option> options = new ArrayList<>();

                  options.add(new PathSelectOption("Select folder", new SimpleObjectProperty<Path>(), PathSelectOption.ONLY_DIRECTORIES_FILTER));

                  return options;
                }
              }));

              return options;
            }
          });
        }
      })
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
          Media media = mediaItem.getMedia();
          MediaData mediaData = mediaItem.get(MediaData.class);

          Map<String, Object> criteria = new HashMap<>();

          criteria.put(SubtitleCriteriaProvider.TITLE, media.getTitle());

          if(media instanceof Movie) {
            Movie movie = (Movie)media;

            criteria.put(SubtitleCriteriaProvider.YEAR, movie.getReleaseYear());
            criteria.put(SubtitleCriteriaProvider.IMDB_ID, movie.getImdbNumber());
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
        public Media createMediaFromFirstItem(MediaItem item) {
          return new Media(item.getTitle(), null, item.getMedia().getReleaseYear());
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
      .setInterface(MovieEnricher.class.getName(), null)
      .setImplementation(MovieEnricher.class)
      .add(createServiceDependency()
        .setService(ItemsDao.class)
        .setRequired(true)
      )
    );

    manager.add(createComponent()
      .setInterface(MainMenuExtension.class.getName(), null)
      .setImplementation(MoviesMainMenuExtension.class)
      .add(createServiceDependency()
        .setService(SelectMediaPresentationProvider.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(MovieEnricher.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(EnrichCache.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(PersistQueue.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(IdentifierDao.class)
        .setRequired(true)
      )
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }

}
