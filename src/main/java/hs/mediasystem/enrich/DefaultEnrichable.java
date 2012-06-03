package hs.mediasystem.enrich;

import java.lang.ref.WeakReference;

public class DefaultEnrichable implements Enrichable {
  private WeakReference<EnrichTrigger> enrichTriggerRef;

  @Override
  public void setEnrichTrigger(EnrichTrigger enrichTrigger) {
    this.enrichTriggerRef = new WeakReference<>(enrichTrigger);
  }

  protected Class<? extends DefaultEnrichable> getEnrichClass() {
    return getClass();
  }

  protected void queueForEnrichment() {
    if(enrichTriggerRef != null) {
      EnrichTrigger enrichTrigger = enrichTriggerRef.get();

      if(enrichTrigger != null) {
        enrichTrigger.queueForEnrichment(getEnrichClass());
      }
    }
  }
}
