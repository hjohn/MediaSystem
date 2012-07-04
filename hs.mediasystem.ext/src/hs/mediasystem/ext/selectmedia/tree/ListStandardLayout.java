package hs.mediasystem.ext.selectmedia.tree;

import hs.mediasystem.screens.selectmedia.AbstractDuoPaneStandardLayout;

import org.osgi.framework.BundleContext;

public class ListStandardLayout extends AbstractDuoPaneStandardLayout {

  public ListStandardLayout(BundleContext bundleContext) {
    super(bundleContext, new TreeListPane(bundleContext));
  }
}