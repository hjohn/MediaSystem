package hs.mediasystem.ext.player.vlc;

import hs.mediasystem.ext.player.vlc.VLCPlayer.Mode;
import hs.mediasystem.framework.player.PlayerFactory;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
//    manager.add(createComponent()
//      .setInterface(PlayerFactory.class.getName(), null)
//      .setImplementation(new VLCPlayerFactory("VLC (seperate window)", Mode.SEPERATE_WINDOW))
//    );
    manager.add(createComponent()
      .setInterface(PlayerFactory.class.getName(), null)
      .setImplementation(new VLCPlayerFactory("VLC (integrated, slower)", Mode.CANVAS))
    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }
}
