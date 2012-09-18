package hs.mediasystem.ext.player.vlc;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.ext.player.vlc.VLCPlayer.Mode;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.player.PlayerFactory;
import hs.mediasystem.screens.AbstractSetting;
import hs.mediasystem.screens.Setting;
import hs.mediasystem.screens.SettingGroup;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.PathSelectOption;
import hs.mediasystem.screens.optiondialog.PathOption;
import hs.mediasystem.util.PathStringConverter;

import java.nio.file.Path;
import java.util.Hashtable;

import javafx.beans.property.ObjectProperty;

import org.apache.felix.dm.DependencyActivatorBase;
import org.apache.felix.dm.DependencyManager;
import org.osgi.framework.BundleContext;

public class Activator extends DependencyActivatorBase {

  @Override
  public void init(BundleContext context, DependencyManager manager) throws Exception {
    manager.add(createComponent()
      .setInterface(Setting.class.getName(), new Hashtable<String, Object>() {{
        put("parentId", "video");
      }})
      .setImplementation(new SettingGroup(context, "video.vlc", "VLC Media Player", 0))
    );

    manager.add(createComponent()
      .setInterface(Setting.class.getName(), new Hashtable<String, Object>() {{
        put("parentId", "video.vlc");
      }})
      .setImplementation(new AbstractSetting("video.vlc.libvlcpath", 0) {
        private volatile SettingsStore settingsStore;

        @Override
        public Option createOption() {
          ObjectProperty<Path> libVlcPath = settingsStore.getProperty("MediaSystem:Ext:Player:VLC", PersistLevel.PERMANENT, "LibVLCPath", new PathStringConverter());

          return new PathOption("Path of libvlc", libVlcPath, PathSelectOption.ONLY_DIRECTORIES_FILTER);
        }
      })
      .add(createServiceDependency()
        .setService(SettingsStore.class)
        .setRequired(true)
      )
    );

    manager.add(createComponent()
      .setInterface(PlayerFactory.class.getName(), null)
      .setImplementation(new VLCPlayerFactory("VLC (seperate window)", Mode.SEPERATE_WINDOW))
      .add(createServiceDependency()
        .setService(SettingsStore.class)
        .setRequired(true)
      )
    );
//    manager.add(createComponent()
//      .setInterface(PlayerFactory.class.getName(), null)
//      .setImplementation(new VLCPlayerFactory("VLC (integrated, slower)", Mode.CANVAS))
//    );
  }

  @Override
  public void destroy(BundleContext context, DependencyManager manager) throws Exception {
  }
}
