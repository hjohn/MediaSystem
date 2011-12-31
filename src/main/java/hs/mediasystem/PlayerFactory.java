package hs.mediasystem;

import hs.mediasystem.framework.player.Player;
import hs.mediasystem.util.ini.Ini;

import java.awt.GraphicsDevice;

public interface PlayerFactory {
  Player create(Ini ini, GraphicsDevice device);
}
