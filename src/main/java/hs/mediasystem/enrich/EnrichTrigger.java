package hs.mediasystem.enrich;


public interface EnrichTrigger {
  void queueForEnrichment(Class<? extends DefaultEnrichable> cls);
}
