package hs.mediasystem.framework;

import hs.mediasystem.enrich.EnrichTrigger;
import hs.mediasystem.enrich.Enrichable;
import hs.mediasystem.persist.PersistTrigger;
import hs.mediasystem.persist.Persistable;

import java.lang.ref.WeakReference;

public class DefaultEnrichable implements Enrichable, Persistable {
  private WeakReference<EnrichTrigger> enrichTriggerRef;
  private WeakReference<PersistTrigger> persistTriggerRef;

  @Override
  public void setEnrichTrigger(EnrichTrigger enrichTrigger) {
    this.enrichTriggerRef = new WeakReference<>(enrichTrigger);
  }

  @Override
  public void setPersistTrigger(PersistTrigger persistTrigger) {
    this.persistTriggerRef = new WeakReference<>(persistTrigger);
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

  protected void queueAsDirty() {
    if(persistTriggerRef != null) {
      PersistTrigger persistTrigger = persistTriggerRef.get();

      if(persistTrigger != null) {
        persistTrigger.queueAsDirty(this);
      }
    }
  }
}
