package hs.mediasystem.framework;

import hs.mediasystem.enrich.EnrichTrigger;
import hs.mediasystem.enrich.Enrichable;
import hs.mediasystem.persist.Persister;
import hs.mediasystem.persist.Persistable;

import java.lang.ref.WeakReference;

public class DefaultEnrichable<P> implements Enrichable, Persistable<P> {
  private WeakReference<EnrichTrigger> enrichTriggerRef;
  private WeakReference<Persister<P>> persistTriggerRef;

  @Override
  public void setEnrichTrigger(EnrichTrigger enrichTrigger) {
    this.enrichTriggerRef = new WeakReference<>(enrichTrigger);
  }

  @Override
  public void setPersister(Persister<P> persistTrigger) {
    this.persistTriggerRef = new WeakReference<>(persistTrigger);
  }

  @SuppressWarnings("unchecked")
  protected Class<? extends DefaultEnrichable<P>> getEnrichClass() {
    return (Class<? extends DefaultEnrichable<P>>)getClass();
  }

  protected void queueForEnrichment() {
    if(enrichTriggerRef != null) {
      EnrichTrigger enrichTrigger = enrichTriggerRef.get();

      if(enrichTrigger != null) {
        enrichTrigger.queueForEnrichment(getEnrichClass());
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected void queueAsDirty() {
    if(persistTriggerRef != null) {
      Persister<P> persistTrigger = persistTriggerRef.get();

      if(persistTrigger != null) {
        persistTrigger.queueAsDirty((P)this);
      }
    }
  }
}
