package hs.mediasystem.ext.player.vlc;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.screens.AbstractSetting;
import hs.mediasystem.screens.Setting;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.PathOption;
import hs.mediasystem.screens.optiondialog.PathSelectOption;
import hs.mediasystem.util.PathStringConverter;

import java.nio.file.Path;
import java.util.Set;

import javafx.beans.property.ObjectProperty;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class VLCPathSetting extends AbstractSetting {
  private final SettingsStore settingsStore;

  @Inject
  public VLCPathSetting(SettingsStore settingsStore) {
    super("video.vlc.libvlcpath", "video.vlc", 0);

    this.settingsStore = settingsStore;
  }

  @Override
  public Option createOption(Set<Setting> settings) {
    ObjectProperty<Path> libVlcPath = settingsStore.getProperty("MediaSystem:Ext:Player:VLC", PersistLevel.PERMANENT, "LibVLCPath", new PathStringConverter());

    return new PathOption("Path of libvlc", libVlcPath, PathSelectOption.ONLY_DIRECTORIES_FILTER);
  }
}
