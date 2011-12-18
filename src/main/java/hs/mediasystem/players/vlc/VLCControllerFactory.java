package hs.mediasystem.players.vlc;

import hs.mediasystem.ControllerFactory;
import hs.mediasystem.ProgramController;
import hs.mediasystem.util.ini.Ini;

import java.awt.GraphicsDevice;

public class VLCControllerFactory implements ControllerFactory {

  @Override
  public ProgramController create(Ini ini, GraphicsDevice device) {
    return new ProgramController(ini, new VLCPlayer(device)); 
  }
}
