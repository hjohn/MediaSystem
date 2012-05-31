package hs.mediasystem.enrich;

import java.lang.ref.WeakReference;

import javafx.beans.WeakListener;

public class WeakEnrichmentListener implements EnrichmentListener, WeakListener {
  private final WeakReference<EnrichmentListener> ref;

  public WeakEnrichmentListener(EnrichmentListener listener) {
    assert listener != null;

    this.ref = new WeakReference<>(listener);
  }

  @Override
  public void update(EnrichmentState state, Class<?> enrichableClass, Object enrichable) {
    EnrichmentListener enrichmentListener = ref.get();

    if(enrichmentListener != null) {
      enrichmentListener.update(state, enrichableClass, enrichable);
    }
  }

  @Override
  public boolean wasGarbageCollected() {
    return ref.get() == null;
  }
}