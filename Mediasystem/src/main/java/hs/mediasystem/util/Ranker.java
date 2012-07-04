package hs.mediasystem.util;

import org.osgi.framework.ServiceReference;

public interface Ranker<S> {
  int rank(ServiceReference<S> ref);
}
