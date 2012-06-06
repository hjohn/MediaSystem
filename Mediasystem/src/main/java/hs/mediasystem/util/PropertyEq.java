package hs.mediasystem.util;

import org.osgi.framework.ServiceReference;

public class PropertyEq {
  private final String name;
  private final Object match;

  public PropertyEq(String name, Object match) {
    this.name = name;
    this.match = match;
  }

  public boolean match(ServiceReference<?> ref) {
    Object value = ref.getProperty(name);

    return match.equals(value);
  }
}
