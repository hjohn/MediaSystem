package hs.mediasystem.enrich;

import java.util.List;

public interface Enricher<T> {
  List<EnrichTask<T>> enrich(Parameters parameters, boolean bypassCache);
  List<Class<?>> getInputTypes();
}
