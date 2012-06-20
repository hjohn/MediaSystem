package hs.mediasystem.util;

import org.osgi.framework.ServiceReference;

public interface PropertyMatcher {
  boolean match(ServiceReference<?> ref);
}
