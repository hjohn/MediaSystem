package hs.mediasystem.enrich;

import java.util.List;
import java.util.Map;

public interface Enricher<K, T> {
  EnrichTaskProvider<T> enrich(K key, Map<Class<?>, Object> inputParameters);
  List<Class<?>> getInputTypes();
}
