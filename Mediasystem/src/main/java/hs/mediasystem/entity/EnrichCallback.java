package hs.mediasystem.entity;

public interface EnrichCallback<P> {
  P enrich(Object... parameters);
}
