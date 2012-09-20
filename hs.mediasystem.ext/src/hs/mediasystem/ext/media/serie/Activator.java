package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.entity.EntityFactory;
import hs.mediasystem.entity.EntityProvider;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemConfigurator;
import hs.mediasystem.framework.MediaProvider;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.SourceImageHandle;
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
import hs.mediasystem.screens.selectmedia.DetailPane;
import hs.mediasystem.screens.selectmedia.DetailPaneDecorator;
import hs.mediasystem.screens.selectmedia.DetailPaneDecoratorFactory;
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
  static final TvdbSerieEnricher TVDB_SERIE_ENRICHER = new TvdbSerieEnricher();
  static final TvdbEpisodeEnricher TVDB_EPISODE_ENRICHER = new TvdbEpisodeEnricher(TVDB_SERIE_ENRICHER);

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(Setting.class.getName(), null)
      .setImplementation(new SettingGroup(context, "series", "Series", 0))
    );

    manager.add(createComponent()
      .setInterface(Setting.class.getName(), new Hashtable<String, Object>() {{
        put("parentId", "series");
      }})
      .setImplementation(new AbstractSetting("series.add-remove", 0) {
        private volatile SettingsStore settingsStore;

        @Override
        public Option createOption() {
          final ObservableList<Path> moviePaths = settingsStore.getListProperty("MediaSystem:Ext:Series", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

          return new PathListOption("Add/Remove Series folder", moviePaths);
        }
      })
      .add(createServiceDependency()
        .setService(SettingsStore.class)
        .setRequired(true)
      )
    );

    manager.add(createComponent()
      .setInterface(MediaNodeCellProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Serie.class);
        put("type", MediaNodeCellProvider.Type.HORIZONTAL);
      }})
      .setImplementation(new MediaNodeCellProvider() {
        @Override
        public MediaNodeCell get() {
          return new BannerCell();
        }
      })
    );

    manager.add(createComponent()
      .setInterface(MediaNodeCellProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Episode.class);
        put("type", MediaNodeCellProvider.Type.HORIZONTAL);
      }})
      .setImplementation(new MediaNodeCellProvider() {
        @Override
        public MediaNodeCell get() {
          return new EpisodeCell();
        }
      })
    );

    manager.add(createComponent()
      .setInterface(SubtitleCriteriaProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Episode.class);
      }})
      .setImplementation(new SubtitleCriteriaProvider() {
        @Override
        public Map<String, Object> getCriteria(MediaItem mediaItem) {
          Media<?> media = mediaItem.getMedia();
          MediaData mediaData = mediaItem.mediaData.get();

          Map<String, Object> criteria = new HashMap<>();

          if(media instanceof Episode) {
            Episode ep = (Episode)media;

            criteria.put(SubtitleCriteriaProvider.TITLE, ep.serie.get().title.get());
            criteria.put(SubtitleCriteriaProvider.SEASON, ep.season.get());
            criteria.put(SubtitleCriteriaProvider.EPISODE, ep.episode.get());
          }

          if(mediaData != null) {
            criteria.put(SubtitleCriteriaProvider.OPEN_SUBTITLES_HASH, mediaData.osHash.get());
            criteria.put(SubtitleCriteriaProvider.FILE_LENGTH, mediaData.fileLength.get());
          }

          return criteria;
        }
      })
    );

    manager.add(createComponent()
      .setInterface(DetailPaneDecoratorFactory.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Episode.class);
      }})
      .setImplementation(new DetailPaneDecoratorFactory() {
        @Override
        public DetailPaneDecorator<?> create(DetailPane.DecoratablePane decoratablePane) {
          return new EpisodeDetailPaneDecorator(decoratablePane);
        }
      })
    );

    manager.add(createComponent()
      .setInterface(MediaGroup.class.getName(), new Hashtable<String, Object>() {{
        put(MediaGroup.Constants.MEDIA_ROOT_CLASS.name(), SerieItem.class);
      }})
      .setImplementation(new DefaultMediaGroup("episodeNumber-group-season", "Season", new SeasonGrouper(), EpisodeComparator.INSTANCE, true, true) {
        @Override
        public Media<?> createMediaFromFirstItem(MediaItem item) {
          Integer season = (Integer)item.properties.get("season");

          return new Media<>(season == null || season == 0 ? "Specials" : "Season " + season);
        }

        @Override
        public String getShortTitle(MediaItem item) {
          Integer season = (Integer)item.properties.get("season");

          return season == null || season == 0 ? "Sp." : "" + season;
        }
      })
    );

    final EntityProvider<Item, Serie> serieEntityProvider = new MediaProvider<Serie>() {
      @Override
      protected Serie createMedia(Item item) {
        if(!item.getProviderId().getType().equals("Serie")) {
          return null;
        }

        return new Serie(item.getTitle());
      }

      @Override
      protected void configureMedia(Serie media, Item item) {
        // TODO Auto-generated method stub

      }
    };

    manager.add(createComponent()
      .setInterface(EntityProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Media.class);
      }})
      .setImplementation(serieEntityProvider)
    );

    manager.add(createComponent()
      .setInterface(EntityProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Media.class);
      }})
      .setImplementation(new MediaProvider<Episode>() {
        private volatile ItemsDao itemsDao;

        @Override
        protected Episode createMedia(Item item) {
          if(!item.getProviderId().getType().equals("Episode")) {
            return null;
          }

          try {
            Item serieItem = itemsDao.loadItem(new ProviderId("Serie", "TVDB", item.getProviderId().getId().split(",")[0]));
            Serie serie = serieEntityProvider.get(serieItem);

            return new Episode(serie, item.getTitle(), item.getSeason(), item.getEpisode(), item.getEpisode());
          }
          catch(ItemNotFoundException e) {
            System.out.println("[FINE] Exception while creating Episode entity: " + e);
            return null;
          }
        }

        @Override
        protected void configureMedia(Episode media, Item item) {
          media.background.set(item.getBackground() == null ? media.serie.get().background.get() : new SourceImageHandle(item.getBackground(), "Episode:/background/" + item.getId()));
        }
      })
      .add(createServiceDependency()
        .setService(ItemsDao.class)
        .setRequired(true)
      )
    );

    manager.add(createComponent()
      .setInterface(MainMenuExtension.class.getName(), null)
      .setImplementation(SeriesMainMenuExtension.class)
      .add(createServiceDependency()
        .setService(PersistQueue.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(MediaItemConfigurator.class)
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
        .setService(SettingsStore.class)
        .setRequired(true)
      )
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }
}
