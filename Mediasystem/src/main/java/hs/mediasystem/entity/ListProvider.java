package hs.mediasystem.entity;

import java.util.concurrent.CompletableFuture;

public interface ListProvider<P extends Entity, K> {
  CompletableFuture<Void> provide(EntityContext context, P parentEntity, K key);
}
