package hs.mediasystem.framework;

public interface EnrichCallback<P> {
  P enrich(Object... parameters);
}
