package hs.mediasystem.entity;

import hs.mediasystem.util.Task;

public interface ListProvider<P extends Entity, K> {
  void provide(EntityContext context, Task enrichTask, P parentEntity, K key);
}
