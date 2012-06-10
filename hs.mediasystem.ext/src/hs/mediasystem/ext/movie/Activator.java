package hs.mediasystem.ext.movie;

import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
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
        put(MediaGroup.Constants.MEDIA_ROOT_CLASS.name(), MoviesMediaTree.class);
      }})
      .setImplementation(new DefaultMediaGroup("Alphabetically, grouped by Title", new MovieGrouper(), MovieTitleGroupingComparator.INSTANCE, false, false) {
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
      .setImplementation(new DefaultMediaGroup("Alphabetically", null, MovieTitleGroupingComparator.INSTANCE, false, false))
    );

    manager.add(createComponent()
      .setInterface(MovieEnricher.class.getName(), null)
      .setImplementation(MovieEnricher.class)
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
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }

}
