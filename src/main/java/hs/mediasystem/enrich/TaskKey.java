package hs.mediasystem.enrich;

import hs.mediasystem.framework.MediaItem;

public class TaskKey {
  private final MediaItem key;
  private final Class<?> enrichableClass;

  public TaskKey(MediaItem key, Class<?> enrichableClass) {
    assert key != null;
    assert enrichableClass != null;

    this.key = key;
    this.enrichableClass = enrichableClass;
  }

  @Override
  public int hashCode() {
    return enrichableClass.hashCode() ^ key.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }

    TaskKey other = (TaskKey)obj;

    return enrichableClass.equals(other.enrichableClass) && key.equals(other.key);
  }

  @Override
  public String toString() {
    return "TaskKey[" + key + " - " + enrichableClass + "]";
  }

  public MediaItem getKey() {
    return key;
  }

  public Class<?> getEnrichableClass() {
    return enrichableClass;
  }
}