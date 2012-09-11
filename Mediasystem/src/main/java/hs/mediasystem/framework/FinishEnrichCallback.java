package hs.mediasystem.framework;

/**
 * Called on JavaFX thread to store results from an enrich chain.
 */
public interface FinishEnrichCallback<R> {
  void update(R result);
}
