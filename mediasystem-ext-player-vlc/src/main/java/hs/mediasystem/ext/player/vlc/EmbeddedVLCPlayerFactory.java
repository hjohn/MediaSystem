package hs.mediasystem.ext.player.vlc;

import javax.inject.Inject;
import javax.inject.Singleton;

import hs.mediasystem.ext.player.vlc.VLCPlayer.Mode;
import hs.mediasystem.framework.SettingsStore;

@Singleton
public class EmbeddedVLCPlayerFactory extends AbstractVLCPlayerFactory {

  @Inject
  public EmbeddedVLCPlayerFactory(SettingsStore settingsStore) {
    super(settingsStore, "VLC (embedded)", Mode.CANVAS);
  }
}
