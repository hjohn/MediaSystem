package hs.mediasystem.entity;

public interface EntityFactory<K> {
  <T extends Entity<?>> T create(Class<T> cls, K key);
  <R extends K> R getAssociatedKey(Entity<?> entity);
}
