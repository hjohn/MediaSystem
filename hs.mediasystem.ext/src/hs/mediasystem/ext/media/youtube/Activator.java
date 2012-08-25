package hs.mediasystem.ext.media.youtube;

import hs.mediasystem.screens.MainMenuExtension;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(MainMenuExtension.class.getName(), null)
      .setImplementation(YouTubeMainMenuExtension.class)
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }
}
