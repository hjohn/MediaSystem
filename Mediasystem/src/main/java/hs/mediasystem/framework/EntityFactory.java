package hs.mediasystem.framework;

public interface EntityFactory {
  <T extends Entity<?>> T create(Class<T> cls, Object... parameters);
}
