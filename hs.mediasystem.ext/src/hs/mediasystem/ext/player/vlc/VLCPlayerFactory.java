package hs.mediasystem.ext.player.vlc;

import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.PlayerFactory;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.NativeLibrary;

public class VLCPlayerFactory implements PlayerFactory {

  @Override
  public Player create(Ini ini) {
    Section main = ini.getSection("vlc");

    NativeLibrary.addSearchPath("libvlc", main.getDefault("libvlcSearchPath", "c:/program files/VideoLAN/VLC"));

    Section section = ini.getSection("vlc.args");
    List<String> args = new ArrayList<>();

    for(String key : section) {
      args.add(key);
      args.add(section.get(key));
    }

    return new VLCPlayer(args.toArray(new String[args.size()]));
  }
}
