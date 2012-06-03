package hs.mediasystem.enrich;

public interface PersistTrigger {
  void queueAsDirty(Persistable persistable);
}
