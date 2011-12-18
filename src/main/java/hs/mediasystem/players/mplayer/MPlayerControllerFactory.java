package hs.mediasystem.players.mplayer;

import hs.mediasystem.ControllerFactory;
import hs.mediasystem.ProgramController;
import hs.mediasystem.util.ini.Ini;

import java.awt.GraphicsDevice;
import java.nio.file.Path;

/**
 * Handles the main window(s) and media playback
 */
public class MPlayerControllerFactory implements ControllerFactory {
  private final Path mplayerPath;

  public MPlayerControllerFactory(Path mplayerPath) {
    this.mplayerPath = mplayerPath;
  }
  
  @Override
  public ProgramController create(Ini ini, GraphicsDevice device) {
    return new ProgramController(ini, new MPlayerPlayer(mplayerPath, true));
  }
}
