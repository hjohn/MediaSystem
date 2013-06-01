package hs.mediasystem.ext.player.vlc;

import hs.mediasystem.screens.SettingGroup;

import javax.inject.Named;

@Named
public class RootSettingGroup extends SettingGroup {

  public RootSettingGroup() {
    super("video.vlc", "video", "VLC Media Player", 0);
  }

}
