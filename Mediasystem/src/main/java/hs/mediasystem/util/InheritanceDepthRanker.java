package hs.mediasystem.util;

import org.osgi.framework.ServiceReference;

public class InheritanceDepthRanker<S> implements Ranker<S> {

  @Override
  public int rank(ServiceReference<S> ref) {
    Class<?> cls = (Class<?>)ref.getProperty("mediasystem.class");
    int depth = 0;

    while((cls = cls.getSuperclass()) != null) {
      depth++;
    }

    return depth;
  }
}
