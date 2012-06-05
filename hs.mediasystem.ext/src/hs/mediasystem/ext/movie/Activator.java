package hs.mediasystem.ext.movie;

import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.persist.Persister;
import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
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
        .setService(Persister.class)
        .setRequired(true)
      )
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }

}
