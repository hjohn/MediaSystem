package hs.mediasystem.dao;

import hs.mediasystem.db.IdClass;
import hs.mediasystem.db.IdColumn;

@IdClass
public class ProviderId {

  @IdColumn(1)
  private final String type;

  @IdColumn(2)
  private final String provider;

  @IdColumn(3)
  private final String id;

  public ProviderId(String type, String provider, String id) {
    assert type != null;
    assert provider != null;
    assert id != null;

    this.type = type;
    this.provider = provider;
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public String getProvider() {
    return provider;
  }

  public String getId() {
    return id;
  }

  @Override
  public String toString() {
    return "ProviderId(" + type + "; " + provider + "; " + id + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + provider.hashCode();
    result = prime * result + id.hashCode();
    result = prime * result + type.hashCode();
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

    ProviderId other = (ProviderId)obj;

    if(!provider.equals(other.provider)) {
      return false;
    }
    if(!id.equals(other.id)) {
      return false;
    }
    if(!type.equals(other.type)) {
      return false;
    }

    return true;
  }
}
