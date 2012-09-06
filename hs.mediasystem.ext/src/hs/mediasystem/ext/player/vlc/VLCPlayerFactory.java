package hs.mediasystem.ext.player.vlc;

import hs.mediasystem.ext.player.vlc.VLCPlayer.Mode;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.PlayerFactory;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.NativeLibrary;

public class VLCPlayerFactory implements PlayerFactory {
  private final String name;
  private final Mode mode;

  public VLCPlayerFactory(String name, Mode mode) {
    this.name = name;
    this.mode = mode;
  }

  @Override
  public Player create(Ini ini) {
    Section main = ini.getSection("vlc");

    if(main != null) {
      NativeLibrary.addSearchPath("libvlc", main.getDefault("libvlcSearchPath", "c:/program files/VideoLAN/VLC"));
    }

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
