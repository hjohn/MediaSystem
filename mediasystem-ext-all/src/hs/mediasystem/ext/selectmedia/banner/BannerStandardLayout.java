package hs.mediasystem.ext.selectmedia.banner;

import hs.mediasystem.screens.selectmedia.AbstractDuoPaneStandardLayout;
import hs.mediasystem.screens.selectmedia.DetailPaneDecoratorFactory;

import java.util.Set;

import javax.inject.Inject;

public class BannerStandardLayout extends AbstractDuoPaneStandardLayout {

  @Inject
  public BannerStandardLayout(BannerListPane bannerListPane, Set<DetailPaneDecoratorFactory> detailPaneDecoratorFactories) {
    super(bannerListPane, detailPaneDecoratorFactories);
  }
}