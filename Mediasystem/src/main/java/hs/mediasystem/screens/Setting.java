package hs.mediasystem.screens;

import java.util.Set;

import hs.mediasystem.screens.optiondialog.Option;

public interface Setting {
  String getId();
  String getParentId();
  double order();
  Option createOption(Set<Setting> settings);
}
