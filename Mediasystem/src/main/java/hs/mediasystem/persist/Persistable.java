package hs.mediasystem.persist;

public interface Persistable<P> {
  void setPersistTrigger(Persister<P> persistTrigger);
}
