package hs.mediasystem.framework;

import hs.mediasystem.persist.Persister;

import java.util.HashMap;
import java.util.Map;

public class PersisterProvider {
  private static final Map<Class<?>, Persister<?>> PERSISTERS = new HashMap<>();

  public static <T> void register(Class<T> cls, Persister<T> persister) {
    PERSISTERS.put(cls, persister);
  }

  @SuppressWarnings("unchecked")
  public static <T> Persister<T> getPersister(Class<T> cls) {
    return (Persister<T>)PERSISTERS.get(cls);
  }
}
