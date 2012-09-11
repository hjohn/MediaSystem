package hs.mediasystem.enrich;

public interface InstanceEnricher<T, R> {
  R enrich(T parent);
  void update(T parent, R result);
}
