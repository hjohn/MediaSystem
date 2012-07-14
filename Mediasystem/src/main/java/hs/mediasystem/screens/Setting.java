package hs.mediasystem.screens;

import hs.mediasystem.screens.optiondialog.Option;

public interface Setting {
  String getId();
  String getTitle();
  String getParentId();
  double order();
  Option createOption();
}
