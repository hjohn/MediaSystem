package hs.mediasystem.players.mplayer;

import hs.mediasystem.PlayerFactory;
import hs.mediasystem.framework.player.Player;
import hs.mediasystem.util.ini.Ini;

import java.awt.GraphicsDevice;
import java.nio.file.Path;

/**
 * Handles the main window(s) and media playback
 */
public class MPlayerPlayerFactory implements PlayerFactory {
  private final Path mplayerPath;

  public MPlayerPlayerFactory(Path mplayerPath) {
    this.mplayerPath = mplayerPath;
  }
  
  @Override
  public Player create(Ini ini, GraphicsDevice device) {
    return new MPlayerPlayer(mplayerPath, true);
  }
}
