package hs.mediasystem.ext.subtitle.sublight;

import hs.mediasystem.framework.SubtitleProvider;

import java.util.Hashtable;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(SubtitleProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediatype", "episode");
      }})
      .setImplementation(SublightSubtitleProvider.class)
    );

    manager.add(createComponent()
      .setInterface(SubtitleProvider.class.getName(), new Hashtable<String, Object>() {{
        put("mediatype", "movie");
      }})
      .setImplementation(SublightSubtitleProvider.class)
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }

}
