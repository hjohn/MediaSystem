package hs.mediasystem.enrich;

import java.util.Map;

public class Parameters {
  private final Map<Class<?>, Object> parameterMap;

  public Parameters(Map<Class<?>, Object> parameterMap) {
    this.parameterMap = parameterMap;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> cls) {
    return (T)parameterMap.get(cls);
  }

  @SuppressWarnings("unchecked")
  public <T> T unwrap(Class<? extends WrappedValue<T>> cls) {
    return ((WrappedValue<T>)parameterMap.get(cls)).get();
  }

  public interface WrappedValue<V> {
    V get();
  }
}
