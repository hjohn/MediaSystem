package hs.mediasystem.enrich;

public interface EnrichmentListener {
  void update(EnrichmentState state, Class<?> enrichableClass, Object enrichable);
}