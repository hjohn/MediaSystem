package hs.mediasystem.players.vlc;

import hs.mediasystem.PlayerFactory;
import hs.mediasystem.framework.player.Player;
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

    int screen = Integer.parseInt(ini.getSection("general").getDefault("screen", "0"));

    return new VLCPlayer(screen, args.toArray(new String[args.size()]));
  }
}
