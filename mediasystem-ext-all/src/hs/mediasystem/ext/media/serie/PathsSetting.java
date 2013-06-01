package hs.mediasystem.ext.media.serie;

import java.nio.file.Path;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import javafx.collections.ObservableList;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.screens.AbstractSetting;
import hs.mediasystem.screens.Setting;
import hs.mediasystem.screens.optiondialog.Option;
import hs.mediasystem.screens.optiondialog.PathListOption;
import hs.mediasystem.util.PathStringConverter;

@Named
public class PathsSetting extends AbstractSetting {
  private final SettingsStore settingsStore;

  @Inject
  public PathsSetting(SettingsStore settingsStore) {
    super("series.add-remove", "series", 0);

    this.settingsStore = settingsStore;
  }

  @Override
  public Option createOption(Set<Setting> settings) {
    final ObservableList<Path> moviePaths = settingsStore.getListProperty("MediaSystem:Ext:Series", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

    return new PathListOption("Add/Remove Series folder", moviePaths);
  }
}
