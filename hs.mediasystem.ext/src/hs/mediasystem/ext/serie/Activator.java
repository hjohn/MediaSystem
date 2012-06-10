package hs.mediasystem.ext.serie;

import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.Episode;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.SerieItem;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.screens.DefaultMediaGroup;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.MediaGroup;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;

import java.util.Hashtable;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(MediaGroup.class.getName(), new Hashtable<String, Object>() {{
        put(MediaGroup.Constants.MEDIA_ROOT_CLASS.name(), SerieItem.class);
      }})
      .setImplementation(new DefaultMediaGroup("Season", new SeasonGrouper(), EpisodeComparator.INSTANCE, true, true) {
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
