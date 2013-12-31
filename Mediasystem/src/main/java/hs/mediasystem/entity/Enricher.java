package hs.mediasystem.entity;

import hs.mediasystem.util.Task;

public interface Enricher<E extends Entity, K> {
  void enrich(EntityContext context, Task currentTask, E entity, K key);
}
