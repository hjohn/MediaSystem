package hs.mediasystem.ext.player.vlc;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.ext.player.vlc.VLCPlayer.Mode;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.PlayerFactory;
import hs.mediasystem.util.PathStringConverter;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import javafx.beans.property.ObjectProperty;

import com.sun.jna.NativeLibrary;

public class VLCPlayerFactory implements PlayerFactory {
  private final SettingsStore settingsStore;
  private final String name;
  private final Mode mode;

  @Inject
  public VLCPlayerFactory(SettingsStore settingsStore) { // TODO: String name, Mode mode -- create 2nd factory for the other mode
    this.settingsStore = settingsStore;
    this.name = "VLC (seperate window)";
    this.mode = Mode.SEPERATE_WINDOW;
  }

  @Override
  public Player create(Ini ini) {
    ObjectProperty<Path> libVlcPathProperty = settingsStore.getProperty("MediaSystem:Ext:Player:VLC", PersistLevel.PERMANENT, "LibVLCPath", new PathStringConverter());

    if(libVlcPathProperty.get() == null) {
      if(System.getProperty("os.arch").equals("x86")) {
        libVlcPathProperty.set(Paths.get("c:/program files (x86)/VideoLAN/VLC"));
      }
      else {
        libVlcPathProperty.set(Paths.get("c:/program files/VideoLAN/VLC"));
      }
    }

    NativeLibrary.addSearchPath("libvlc", libVlcPathProperty.get().toString());

    List<String> args = new ArrayList<>();
    Section vlcArgsSection = ini.getSection("vlc.args");

    if(vlcArgsSection != null) {
      for(String key : vlcArgsSection) {
        args.add(key);
        args.add(vlcArgsSection.get(key));
      }
    }

    return new VLCPlayer(mode, args.toArray(new String[args.size()]));
  }

  @Override
  public String getName() {
    return name;
  }
}
