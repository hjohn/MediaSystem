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

import uk.co.caprica.vlcj.logger.Logger;
import uk.co.caprica.vlcj.logger.Logger.Level;
import javafx.beans.property.ObjectProperty;

import com.sun.jna.NativeLibrary;

public abstract class AbstractVLCPlayerFactory implements PlayerFactory {
  private final SettingsStore settingsStore;
  private final String name;
  private final Mode mode;

  public AbstractVLCPlayerFactory(SettingsStore settingsStore, String name, Mode mode) {
    this.settingsStore = settingsStore;
    this.name = name;
    this.mode = mode;
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
    Logger.setLevel(Level.INFO);

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
