package hs.mediasystem.entity;

import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.persist.Persistable;
import hs.mediasystem.persist.DatabasePersister;

import java.lang.ref.WeakReference;

public class DefaultPersistable<P> extends DatabaseObject implements Persistable<P> {
  private WeakReference<DatabasePersister<P>> persistTriggerRef;

  @Override
  public void setPersister(DatabasePersister<P> persistTrigger) {
    this.persistTriggerRef = new WeakReference<>(persistTrigger);
  }

  @SuppressWarnings("unchecked")
  protected void queueAsDirty() {
    if(persistTriggerRef != null) {
      DatabasePersister<P> persistTrigger = persistTriggerRef.get();

      if(persistTrigger != null) {
        persistTrigger.queueAsDirty((P)this);
      }
    }
  }
}
