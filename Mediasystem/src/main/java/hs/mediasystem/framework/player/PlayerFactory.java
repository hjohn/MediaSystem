package hs.mediasystem.framework.player;

import hs.mediasystem.util.ini.Ini;

public interface PlayerFactory {
  Player create(Ini ini);
}
