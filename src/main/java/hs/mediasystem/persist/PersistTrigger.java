package hs.mediasystem.persist;

public interface PersistTrigger {
  void queueAsDirty(Persistable persistable);
}
