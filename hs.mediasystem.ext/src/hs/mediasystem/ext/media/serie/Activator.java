package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.dao.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SubtitleCriteriaProvider;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.DefaultMediaGroup;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.selectmedia.DetailPane;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(SubtitleCriteriaProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Episode.class);
      }})
      .setImplementation(new SubtitleCriteriaProvider() {
        @Override
        public Map<String, Object> getCriteria(MediaItem mediaItem) {
          Media media = mediaItem.getMedia();
          MediaData mediaData = mediaItem.get(MediaData.class);

          Map<String, Object> criteria = new HashMap<>();

          if(media instanceof Episode) {
            Episode ep = (Episode)media;

            criteria.put(SubtitleCriteriaProvider.TITLE, ep.getSerie().getTitle());
            criteria.put(SubtitleCriteriaProvider.SEASON, ep.getSeason());
            criteria.put(SubtitleCriteriaProvider.EPISODE, ep.getEpisode());
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
      .setInterface(DetailPane.class.getName(), new Hashtable<String, Object>() {{
        put("mediasystem.class", Episode.class);
      }})
      .setImplementation(EpisodeDetailPane.class)
    );

    manager.add(createComponent()
      .setInterface(MediaGroup.class.getName(), new Hashtable<String, Object>() {{
        put(MediaGroup.Constants.MEDIA_ROOT_CLASS.name(), SerieItem.class);
      }})
      .setImplementation(new DefaultMediaGroup("episodeNumber-group-season", "Season", new SeasonGrouper(), EpisodeComparator.INSTANCE, true, true) {
        @Override
        public Media createMediaFromFirstItem(MediaItem item) {
          Integer season = item.get(Episode.class).getSeason();

          return new Media(season == null || season == 0 ? "Specials" : "Season " + season);
        }

        @Override
        public String getShortTitle(MediaItem item) {
          Integer season = item.get(Episode.class).getSeason();

          return season == null || season == 0 ? "Sp." : "" + season;
        }
      })
    );

    manager.add(createComponent()
      .setInterface(SerieEnricher.class.getName(), null)
      .setImplementation(SerieEnricher.class)
      .add(createServiceDependency()
        .setService(ItemsDao.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(TypeBasedItemEnricher.class)
        .setRequired(true)
      )
    );

    manager.add(createComponent()
      .setInterface(EpisodeEnricher.class.getName(), null)
      .setImplementation(EpisodeEnricher.class)
      .add(createServiceDependency()
        .setService(ItemsDao.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(TypeBasedItemEnricher.class)
        .setRequired(true)
      )
    );

    manager.add(createComponent()
      .setInterface(MainMenuExtension.class.getName(), null)
      .setImplementation(SeriesMainMenuExtension.class)
      .add(createServiceDependency()
        .setService(SelectMediaPresentationProvider.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(SerieEnricher.class)
        .setRequired(true)
      )
      .add(createServiceDependency()
        .setService(EpisodeEnricher.class)
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
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }

}
