package hs.mediasystem.util;

import java.util.Comparator;

public class ClassInheritanceDepthComparator implements Comparator<Class<?>> {
  public static final ClassInheritanceDepthComparator INSTANCE = new ClassInheritanceDepthComparator();

  @Override
  public int compare(Class<?> c1, Class<?> c2) {
    if(c1.equals(c2)) {
      return 0;
    }
    if(c1.isAssignableFrom(c2)) {
      return -1;
    }
    if(c2.isAssignableFrom(c1)) {
      return 1;
    }

    throw new IllegalArgumentException("classes to compare must be in the same inheritance tree: " + c1 + "; " + c2);
  }
}
