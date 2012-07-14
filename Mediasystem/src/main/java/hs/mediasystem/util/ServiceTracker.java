package hs.mediasystem.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServiceTracker<S> {
  private final org.osgi.util.tracker.ServiceTracker<S, S> tracker;
  private final BundleContext bundleContext;
  private final PropertyMatcher[] defaultFilters;
  private final Comparator<S> defaultOrder;
  private final Ranker<S> ranker;

  public ServiceTracker(BundleContext bundleContext, Class<S> serviceClass, Comparator<S> defaultOrder, Ranker<S> ranker, PropertyMatcher... defaultFilters) {
    this.bundleContext = bundleContext;
    this.defaultOrder = defaultOrder;
    this.ranker = ranker;
    this.defaultFilters = defaultFilters.clone();

    tracker = new org.osgi.util.tracker.ServiceTracker<>(bundleContext, serviceClass, null);
    tracker.open();
  }

  public ServiceTracker(BundleContext bundleContext, Class<S> serviceClass, Ranker<S> ranker, PropertyMatcher... defaultFilters) {
    this(bundleContext, serviceClass, null, ranker, defaultFilters);
  }

  public ServiceTracker(BundleContext bundleContext, Class<S> serviceClass, Comparator<S> defaultOrder, PropertyMatcher... defaultFilters) {
    this(bundleContext, serviceClass, defaultOrder, null, defaultFilters);
  }

  public ServiceTracker(BundleContext bundleContext, Class<S> serviceClass, PropertyMatcher... defaultFilters) {
    this(bundleContext, serviceClass, null, null, defaultFilters);
  }

  @SuppressWarnings("unchecked")
  public ServiceReference<S>[] getServiceReferences() {
    ServiceReference<S>[] serviceReferences = tracker.getServiceReferences();

    return serviceReferences == null ? new ServiceReference[0] : serviceReferences;
  }

  public S getService(PropertyMatcher... filters) {
    List<ServiceReference<S>> serviceReferences = getServiceReferencesUnordered(filters);

    if(serviceReferences.isEmpty()) {
      return null;
    }
    if(serviceReferences.size() == 1 || ranker == null) {
      return bundleContext.getService(serviceReferences.get(0));
    }

    ServiceReference<S> bestRef = null;
    int bestRank = Integer.MIN_VALUE;

    for(ServiceReference<S> ref : serviceReferences) {
      int rank = ranker.rank(ref);

      if(rank > bestRank) {
        bestRank = rank;
        bestRef = ref;
      }
    }

    return bundleContext.getService(bestRef);
  }

  public List<S> getServices(Comparator<S> order, PropertyMatcher... filters) {
    List<S> services = getServicesUnordered(filters);

    Collections.sort(services, order);

    return services;
  }

  public List<S> getServices(PropertyMatcher... filters) {
    List<S> services = getServicesUnordered(filters);

    if(defaultOrder != null) {
      Collections.sort(services, defaultOrder);
    }

    return services;
  }

  private List<S> getServicesUnordered(PropertyMatcher... filters) {
    List<S> services = new ArrayList<>();

    ServiceReference<S>[] serviceReferences = tracker.getServiceReferences();

    if(serviceReferences != null) {
      nextRef:
      for(ServiceReference<S> ref : serviceReferences) {
        for(PropertyMatcher filter : defaultFilters) {
          if(!filter.match(ref)) {
            continue nextRef;
          }
        }

        for(PropertyMatcher filter : filters) {
          if(!filter.match(ref)) {
            continue nextRef;
          }
        }

        services.add(bundleContext.getService(ref));
      }
    }

    return services;
  }

  private List<ServiceReference<S>> getServiceReferencesUnordered(PropertyMatcher... filters) {
    List<ServiceReference<S>> services = new ArrayList<>();

    ServiceReference<S>[] serviceReferences = tracker.getServiceReferences();

    if(serviceReferences != null) {
      nextRef:
      for(ServiceReference<S> ref : serviceReferences) {
        for(PropertyMatcher filter : defaultFilters) {
          if(!filter.match(ref)) {
            continue nextRef;
          }
        }

        for(PropertyMatcher filter : filters) {
          if(!filter.match(ref)) {
            continue nextRef;
          }
        }

        services.add(ref);
      }
    }

    return services;
  }
}
