package hs.mediasystem.screens;

import hs.mediasystem.util.PropertyEq;
import hs.mediasystem.util.Ranker;
import hs.mediasystem.util.ServiceTracker;

import javax.inject.Provider;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public interface MediaNodeCellProvider extends Provider<MediaNodeCell> {
  public enum Type {
    HORIZONTAL, VERTICAL, SQUARE;

    public static Ranker<MediaNodeCellProvider> RANKER = new Ranker<MediaNodeCellProvider>() {
      @Override
      public int rank(ServiceReference<MediaNodeCellProvider> ref) {
        Class<?> cls = (Class<?>)ref.getProperty("mediasystem.class");
        int depth = 0;

        while((cls = cls.getSuperclass()) != null) {
          depth++;
        }

        return depth;
      }
    };

    public ServiceTracker<MediaNodeCellProvider> createTracker(BundleContext bundleContext) {
      return new ServiceTracker<>(bundleContext, MediaNodeCellProvider.class, RANKER, new PropertyEq("type", this));
    }
  }
}
