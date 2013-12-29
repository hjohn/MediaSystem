package hs.mediasystem.persist;

public interface DatabasePersister<P> {
  void queueAsDirty(P persistable);
}
