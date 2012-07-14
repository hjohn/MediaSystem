package hs.mediasystem.screens;

import hs.mediasystem.screens.optiondialog.Option;

public interface Setting {
  String getId();
  double order();
  Option createOption();
}
