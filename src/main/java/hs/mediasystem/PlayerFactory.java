package hs.mediasystem;

import hs.mediasystem.framework.player.Player;
import hs.mediasystem.util.ini.Ini;

public interface PlayerFactory {
  Player create(Ini ini);
}
