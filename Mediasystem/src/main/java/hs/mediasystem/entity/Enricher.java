package hs.mediasystem.entity;

import java.util.concurrent.CompletableFuture;

public interface Enricher<E extends Entity, K> {
  CompletableFuture<Void> enrich(EntityContext context, E entity, K key);
}
