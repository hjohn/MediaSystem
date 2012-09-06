package hs.mediasystem.framework.player;

import hs.mediasystem.util.ini.Ini;

public interface PlayerFactory {
  String getName();
  Player create(Ini ini);
}
