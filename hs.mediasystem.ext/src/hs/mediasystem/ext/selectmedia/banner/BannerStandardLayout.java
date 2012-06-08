package hs.mediasystem.ext.selectmedia.banner;

import hs.mediasystem.screens.selectmedia.AbstractDuoPaneStandardLayout;
import hs.mediasystem.screens.selectmedia.DetailPane;

public class BannerStandardLayout extends AbstractDuoPaneStandardLayout {

  public BannerStandardLayout() {
    super(new BannerListPane(), new DetailPane());
  }
}