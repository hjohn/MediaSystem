package hs.mediasystem.entity;

public class EntitySource {
  private final String name;
  private final double priority;
  private final Class<?> keyClass;

  public EntitySource(String name, double priority, Class<?> keyClass) {
    this.name = name;
    this.priority = priority;
    this.keyClass = keyClass;
  }

  public String getName() {
    return name;
  }

  public double getPriority() {
    return priority;
  }

  public Class<?> getKeyClass() {
    return keyClass;
  }

  @Override
  public String toString() {
    return "EntitySource[" + name + "]";
  }
}
