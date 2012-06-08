package hs.mediasystem.ext.selectmedia.tree;

import hs.mediasystem.screens.selectmedia.StandardLayoutExtension;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(StandardLayoutExtension.class.getName(), null)
      .setImplementation(ListStandardLayoutExtension.class)
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }
}
