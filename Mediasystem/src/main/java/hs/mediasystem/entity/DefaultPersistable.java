package hs.mediasystem.entity;

import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.persist.Persistable;
import hs.mediasystem.persist.Persister;

import java.lang.ref.WeakReference;

public class DefaultPersistable<P> extends DatabaseObject implements Persistable<P> {
  private WeakReference<Persister<P>> persistTriggerRef;

  @Override
  public void setPersister(Persister<P> persistTrigger) {
    this.persistTriggerRef = new WeakReference<>(persistTrigger);
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
