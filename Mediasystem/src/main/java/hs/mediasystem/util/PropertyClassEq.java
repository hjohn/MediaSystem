package hs.mediasystem.util;

import org.osgi.framework.ServiceReference;

public class PropertyClassEq implements PropertyMatcher {
  private final String name;
  private final Class<?> cls;

  public PropertyClassEq(String name, Class<?> cls) {
    this.name = name;
    this.cls = cls;
  }

  @Override
  public boolean match(ServiceReference<?> ref) {
    Class<?> propertyClass = (Class<?>)ref.getProperty(name);

    return propertyClass.isAssignableFrom(cls);
  }
}
