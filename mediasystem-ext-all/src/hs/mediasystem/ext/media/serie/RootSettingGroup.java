package hs.mediasystem.ext.media.serie;

import hs.mediasystem.screens.SettingGroup;

import javax.inject.Named;

@Named
public class RootSettingGroup extends SettingGroup {

  public RootSettingGroup() {
    super("series", null, "Series", 0);
  }

}
