package hs.mediasystem.framework;

import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.persist.PersistQueue;

/**
 * A representation of a group of (related) media with navigation and display information.
 */
public interface MediaTree {
  EnrichCache getEnrichCache();
  PersistQueue getPersister();
}
