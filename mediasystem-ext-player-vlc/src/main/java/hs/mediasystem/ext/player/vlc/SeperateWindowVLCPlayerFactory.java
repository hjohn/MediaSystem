package hs.mediasystem.ext.player.vlc;

import javax.inject.Inject;
import javax.inject.Singleton;

import hs.mediasystem.ext.player.vlc.VLCPlayer.Mode;
import hs.mediasystem.framework.SettingsStore;

@Singleton
public class SeperateWindowVLCPlayerFactory extends AbstractVLCPlayerFactory {

  @Inject
  public SeperateWindowVLCPlayerFactory(SettingsStore settingsStore) {
    super(settingsStore, "VLC (seperate window)", Mode.SEPERATE_WINDOW);
  }
}
