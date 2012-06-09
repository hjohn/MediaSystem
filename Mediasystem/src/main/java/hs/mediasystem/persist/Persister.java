package hs.mediasystem.persist;

public interface Persister<P> {
  void queueAsDirty(P persistable);
}
