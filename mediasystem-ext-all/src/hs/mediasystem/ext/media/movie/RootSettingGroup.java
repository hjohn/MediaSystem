package hs.mediasystem.ext.media.movie;

import hs.mediasystem.screens.SettingGroup;

import javax.inject.Named;

@Named
public class RootSettingGroup extends SettingGroup {

  public RootSettingGroup() {
    super("movies", null, "Movies", 0);
  }

}
