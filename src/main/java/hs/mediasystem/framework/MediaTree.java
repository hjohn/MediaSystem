package hs.mediasystem.framework;

import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.persist.Persister;

/**
 * A representation of a group of (related) media with navigation and display information.
 */
public interface MediaTree {
  EnrichCache getEnrichCache();
  Persister getPersister();
}
