package hs.mediasystem.enrich;

public interface EntityEnricher<T, P> {
  P enrich(T parent);
}
