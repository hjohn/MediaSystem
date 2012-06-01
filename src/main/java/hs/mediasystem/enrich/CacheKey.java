package hs.mediasystem.enrich;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

public class CacheKey {
  private static final AtomicLong PRIORITY = new AtomicLong(0);

  private final String[] keys;

  private long priority;

  public CacheKey(String... keys) {
    assert keys != null && keys.length > 0;

    this.keys = keys.clone();
    this.priority = PRIORITY.incrementAndGet();
  }

  public long getPriority() {
    return priority;
  }

  public void promote() {
    this.priority = PRIORITY.incrementAndGet();
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(keys);
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }

    CacheKey other = (CacheKey)obj;

    return Arrays.equals(keys, other.keys);
  }

  @Override
  public String toString() {
    return "CacheKey[" + Arrays.toString(keys) + "; p=" + priority + "]";
  }
}
