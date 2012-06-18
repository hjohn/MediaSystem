package hs.mediasystem.util;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServiceTracker<S> {
  private final org.osgi.util.tracker.ServiceTracker<S, S> tracker;
  private final BundleContext bundleContext;

  public ServiceTracker(BundleContext bundleContext, Class<S> serviceClass) {
    this.bundleContext = bundleContext;
    tracker = new org.osgi.util.tracker.ServiceTracker<>(bundleContext, serviceClass, null);
    tracker.open();
  }

  @SuppressWarnings("unchecked")
  public ServiceReference<S>[] getServiceReferences() {
    ServiceReference<S>[] serviceReferences = tracker.getServiceReferences();

    return serviceReferences == null ? new ServiceReference[0] : serviceReferences;
  }

  public S getService(PropertyEq... filters) {
    List<S> services = getServices(filters);

    return services.isEmpty() ? null : services.get(0);
  }

  public List<S> getServices(PropertyEq... filters) {
    List<S> services = new ArrayList<>();

    ServiceReference<S>[] serviceReferences = tracker.getServiceReferences();

    if(serviceReferences != null) {
      nextRef:
      for(ServiceReference<S> ref : serviceReferences) {
        for(PropertyEq filter : filters) {
          if(!filter.match(ref)) {
            continue nextRef;
          }
        }

        services.add(bundleContext.getService(ref));
      }
    }

    return services;
  }
}
