package hs.mediasystem.framework.descriptors;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class Descriptor {
  private final String name;
  private final double uniqueness;
  private final Object type;
  private final Set<Descriptor> elements;

  public Descriptor(String name, double uniqueness, Object type, Descriptor... elements) {
    this.name = name;
    this.uniqueness = uniqueness;
    this.type = type;
    this.elements = ImmutableSet.copyOf(elements);
  }

  public Descriptor(String name, Object type, Descriptor... elements) {
    this(name, determineUniqueness(elements), type, elements);

    if(elements.length == 0) {
      throw new IllegalArgumentException("parameter 'elements' cannot be empty");
    }
  }

  private static double determineUniqueness(Descriptor[] elements) {
    double inversedUniqueness = 1;

    for(Descriptor property : elements) {
      inversedUniqueness *= 1 - property.getUniqueness();
    }

    return 1 - inversedUniqueness;
  }

  public String getName() {
    return name;
  }

  public Object getType() {
    return type;
  }

  public double getUniqueness() {
    return uniqueness;
  }

  public Set<Descriptor> getElements() {
    return elements;
  }
}