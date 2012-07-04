package hs.mediasystem.ext.selectmedia.banner;

import hs.mediasystem.screens.selectmedia.AbstractDuoPaneStandardLayout;

import org.osgi.framework.BundleContext;

public class BannerStandardLayout extends AbstractDuoPaneStandardLayout {

  public BannerStandardLayout(BundleContext bundleContext) {
    super(bundleContext, new BannerListPane(bundleContext));
  }
}