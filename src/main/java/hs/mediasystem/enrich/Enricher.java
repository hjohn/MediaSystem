package hs.mediasystem.enrich;

import java.util.List;
import java.util.Map;

public interface Enricher<K, T> {
  List<EnrichTask<T>> enrich(K key, Map<Class<?>, Object> inputParameters, boolean bypassCache);
  List<Class<?>> getInputTypes();
}
