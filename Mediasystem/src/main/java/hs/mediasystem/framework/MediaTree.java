package hs.mediasystem.framework;

import hs.mediasystem.persist.PersistQueue;

/**
 * A representation of a group of (related) media with navigation and display information.
 */
public interface MediaTree {
  PersistQueue getPersister();
}
