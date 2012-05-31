package hs.mediasystem.enrich;

import java.util.Arrays;

public class CacheKey {
  private final String[] keys;

  public CacheKey(String... keys) {
    assert keys != null && keys.length > 0;

    this.keys = keys.clone();
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
}
