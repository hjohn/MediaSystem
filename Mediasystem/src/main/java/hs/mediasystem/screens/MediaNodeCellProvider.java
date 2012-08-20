package hs.mediasystem.screens;

import hs.mediasystem.util.InheritanceDepthRanker;
import hs.mediasystem.util.PropertyEq;
import hs.mediasystem.util.Ranker;
import hs.mediasystem.util.ServiceTracker;

import javax.inject.Provider;

import org.osgi.framework.BundleContext;

public interface MediaNodeCellProvider extends Provider<MediaNodeCell> {
  public enum Type {
    HORIZONTAL, VERTICAL, SQUARE;

    private static Ranker<MediaNodeCellProvider> RANKER = new InheritanceDepthRanker<>();

    public ServiceTracker<MediaNodeCellProvider> createTracker(BundleContext bundleContext) {
      return new ServiceTracker<>(bundleContext, MediaNodeCellProvider.class, RANKER, new PropertyEq("type", this));
    }
  }
}
