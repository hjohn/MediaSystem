package hs.mediasystem.entity;

public class SourceKey {
  private final EntitySource source;
  private final Object key;

  public SourceKey(EntitySource source, Object key) {
    if(source == null) {
      throw new IllegalArgumentException("parameter 'source' cannot be null");
    }

    this.source = source;
    this.key = key;
  }

  public EntitySource getSource() {
    return source;
  }

  public Object getKey() {
    return key;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + source.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }

    SourceKey other = (SourceKey)obj;
    if(key == null) {
      if(other.key != null) {
        return false;
      }
    }
    else if(!key.equals(other.key)) {
      return false;
    }
    if(!source.equals(other.source)) {
      return false;
    }
    return true;
  }
}
