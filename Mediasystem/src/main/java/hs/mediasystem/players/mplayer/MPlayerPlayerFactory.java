package hs.mediasystem.players.mplayer;

import hs.mediasystem.framework.player.Player;
import hs.mediasystem.framework.player.PlayerFactory;
import hs.mediasystem.util.ini.Ini;

import java.nio.file.Path;

/**
 * Handles the main window(s) and media playback.
 */
public class MPlayerPlayerFactory implements PlayerFactory {
  private final Path mplayerPath;

  public MPlayerPlayerFactory(Path mplayerPath) {
    this.mplayerPath = mplayerPath;
  }

  @Override
  public Player create(Ini ini) {
    return new MPlayerPlayer(mplayerPath, true);
  }

  @Override
  public String getName() {
    return "MPlayer";
  }
}
