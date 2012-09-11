package hs.mediasystem.enrich;

import java.util.List;

public interface ListEnricher<T, P> {
  List<P> enrich(T parent);
}
