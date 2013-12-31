package hs.mediasystem.persist;

import hs.mediasystem.entity.Entity;

public interface Persister<E extends Entity, K> {
  void persist(E entity, K key);
}
