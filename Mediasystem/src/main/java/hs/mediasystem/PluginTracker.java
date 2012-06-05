package hs.mediasystem;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class PluginTracker<T> extends ServiceTracker<T, T> {
  private final ObservableList<T> plugins = FXCollections.observableArrayList();
  public ObservableList<T> getPlugins() { return plugins; }

  private final BundleContext bundleContext;

  public PluginTracker(BundleContext bundleContext, Class<T> cls) {
    super(bundleContext, cls, null);

    this.bundleContext = bundleContext;

    open();
  }

  @Override
  public T addingService(ServiceReference<T> reference) {
    final T service = bundleContext.getService(reference);

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        plugins.add(service);
      }
    });

    return service;
  }

  @Override
  public void removedService(ServiceReference<T> reference, final T service) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        plugins.remove(service);
      }
    });
  }
}
