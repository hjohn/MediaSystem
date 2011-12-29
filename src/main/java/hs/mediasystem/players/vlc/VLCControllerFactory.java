package hs.mediasystem.players.vlc;

import hs.mediasystem.ControllerFactory;
import hs.mediasystem.ProgramController;
import hs.mediasystem.util.ini.Ini;
import hs.mediasystem.util.ini.Section;

import java.awt.GraphicsDevice;
import java.util.ArrayList;
import java.util.List;

import com.sun.jna.NativeLibrary;

public class VLCControllerFactory implements ControllerFactory {

  @Override
  public ProgramController create(Ini ini, GraphicsDevice device) {
    Section main = ini.getSection("vlc");
    
    NativeLibrary.addSearchPath("libvlc", main.getDefault("libvlcSearchPath", "c:/program files/VideoLAN/VLC"));
    
    Section section = ini.getSection("vlc.args");
    List<String> args = new ArrayList<String>();
    
    for(String key : section) {
      args.add(key);
      args.add(section.get(key));
    }
    
    return new ProgramController(ini, new VLCPlayer(device, args.toArray(new String[args.size()]))); 
  }
}
