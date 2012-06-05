package hs.mediasystem.ext.nos;

import hs.mediasystem.screens.MainMenuExtension;
import hs.mediasystem.screens.selectmedia.SelectMediaPresentationProvider;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(MainMenuExtension.class.getName(), null)
      .setImplementation(NosMainMenuExtension.class)
      .add(createServiceDependency()
        .setService(SelectMediaPresentationProvider.class)
        .setRequired(true)
      )
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }

}
