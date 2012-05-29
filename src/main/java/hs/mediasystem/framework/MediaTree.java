package hs.mediasystem.framework;

import hs.mediasystem.enrich.EnrichCache;

/**
 * A representation of a group of (related) media with navigation and display information.
 */
public interface MediaTree {
  EnrichCache<MediaItem> getEnrichCache();
}
