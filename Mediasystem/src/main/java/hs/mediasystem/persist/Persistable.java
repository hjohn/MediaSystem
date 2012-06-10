package hs.mediasystem.persist;

public interface Persistable<P> {
  void setPersister(Persister<P> persistTrigger);
}
