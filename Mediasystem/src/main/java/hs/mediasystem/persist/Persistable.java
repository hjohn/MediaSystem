package hs.mediasystem.persist;

public interface Persistable<P> {
  void setPersister(DatabasePersister<P> persistTrigger);
}
