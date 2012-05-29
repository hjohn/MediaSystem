package hs.mediasystem.enrich;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EnrichCache<K> {
  private final Map<Class<?>, Enricher<K, ?>> ENRICHERS = new HashMap<>();

  private final Map<K, Map<Class<?>, CacheValue>> cache = new WeakHashMap<>();
  private final Map<K, Set<EnrichmentListener>> enrichmentListeners = new WeakHashMap<>();  // TODO EnrichmentListener refs must be weak
  private final Map<K, Set<PendingEnrichment>> pendingEnrichmentMap = new WeakHashMap<>();

  private final EnrichmentHandler enrichmentHandler;

  public <T> void registerEnricher(Class<T> cls, Enricher<K, T> enricher) {
    ENRICHERS.put(cls, enricher);
  }

  @Inject
  public EnrichCache(EnrichmentHandler enrichmentHandler) {
    this.enrichmentHandler = enrichmentHandler;
  }

  public void addListener(K key, EnrichmentListener listener) {
    synchronized(cache) {
      Set<EnrichmentListener> listeners = enrichmentListeners.get(key);

      if(listeners == null) {
        listeners = new HashSet<>();
        enrichmentListeners.put(key, listeners);
      }

      listeners.add(listener);

      Map<Class<?>, CacheValue> cacheValues = cache.get(key);

      if(cacheValues != null) {
        for(Map.Entry<Class<?>, CacheValue> entry : cacheValues.entrySet()) {
          listener.update(entry.getValue().state, entry.getKey(), entry.getValue().enrichable);
        }
      }
    }
  }

  public void removeListener(K key, EnrichmentListener listener) {
    synchronized(cache) {
      Set<EnrichmentListener> listeners = enrichmentListeners.get(key);

      if(listeners != null) {
        listeners.remove(listener);
      }
    }
  }

  public CacheValue getCacheValue(K key, Class<?> enrichableClass) {
    synchronized(cache) {
      Map<Class<?>, CacheValue> cacheValues = cache.get(key);

      if(cacheValues == null) {
        return null;
      }

      return cacheValues.get(enrichableClass);
    }
  }

  public CacheValue getDescendantCacheValue(K key, Class<?> enrichableClass) {
    synchronized(cache) {
      Map<Class<?>, CacheValue> cacheValues = cache.get(key);

      if(cacheValues == null) {
        return null;
      }

      for(Class<?> cls : cacheValues.keySet()) {
        if(enrichableClass.isAssignableFrom(cls)) {
          return cacheValues.get(cls);
        }
      }

      return null;
    }
  }

  public <T> void enrich(Class<T> enrichableClass, K key) {
    // System.out.println("[FINE] Enrichers.enrich() - " + enrichableClass.getName() + ": " + key);

    synchronized(cache) {
      @SuppressWarnings("unchecked")
      Enricher<K, T> enricher = (Enricher<K, T>)ENRICHERS.get(enrichableClass);

      if(enricher == null) {
        System.out.println("No suitable Enricher for " + enrichableClass);
        new Exception().printStackTrace();
        insert(key, EnrichmentState.ENRICHMENT_FAILED, enrichableClass, null);
      }
      else {
        PendingEnrichment pendingEnrichment = new PendingEnrichment(enricher, enrichableClass);

        /*
         * First check if this Enrichment is not already pending.  Although the system has no problem handling
         * multiple the same Pending Enrichments (as after the first one is being processed the others only
         * result in a promotion) it can be wasteful to have many of these queued up.
         */

        Set<PendingEnrichment> pendingEnrichments = pendingEnrichmentMap.get(key);

        if(pendingEnrichments != null && pendingEnrichments.contains(pendingEnrichment)) {

          /*
           * The Pending Enrichment already exists, just discard.
           */

          return;
        }

        /*
         * Only if this unique Enrichment cannot be processed immediately, we'll add it to the PENDING_ENRICHMENTS map.
         */

        PendingEnrichmentAction action = pendingEnrichment.enrichIfConditionsMet(key);

        if(action == PendingEnrichmentAction.ENQUEUE) {
          if(pendingEnrichments == null) {
            pendingEnrichments = new HashSet<>();
            pendingEnrichmentMap.put(key, pendingEnrichments);
          }

          pendingEnrichments.add(pendingEnrichment);
        }
        else if(action == PendingEnrichmentAction.FAIL) {
          insert(key, EnrichmentState.ENRICHMENT_FAILED, pendingEnrichment.getEnrichableClass(), null);
        }
      }
    }
  }

  public void insert(K key, EnrichmentState state, Class<?> enrichableClass, Object enrichable) {
    synchronized(cache) {
      Map<Class<?>, CacheValue> cacheValues = cache.get(key);

      if(cacheValues == null) {
        cacheValues = new HashMap<>();
        cache.put(key, cacheValues);
      }

      cacheValues.put(enrichableClass, new CacheValue(state, enrichable));

      Set<EnrichmentListener> listeners = enrichmentListeners.get(key);

      if(listeners != null) {
        for(EnrichmentListener listener : new HashSet<>(listeners)) {
          listener.update(state, enrichableClass, enrichable);
        }
      }

      Set<PendingEnrichment> pendingEnrichments = pendingEnrichmentMap.get(key);

      if(pendingEnrichments != null) {
        for(PendingEnrichment pendingEnrichment : new HashSet<>(pendingEnrichments)) {
          PendingEnrichmentAction action = pendingEnrichment.enrichIfConditionsMet(key);

          if(action == PendingEnrichmentAction.ENRICHED || action == PendingEnrichmentAction.FAIL) {
            pendingEnrichments.remove(pendingEnrichment);

            if(pendingEnrichments.isEmpty()) {
              pendingEnrichmentMap.remove(key);
            }

            if(action == PendingEnrichmentAction.FAIL) {
              insert(key, EnrichmentState.ENRICHMENT_FAILED, pendingEnrichment.getEnrichableClass(), null);
            }
          }
        }
      }
    }
  }

  public <E> E getFromCache(K key, Class<E> enrichableClass) {
    synchronized(cache) {
      CacheValue cacheValue = getCacheValue(key, enrichableClass);

      @SuppressWarnings("unchecked")
      E e = (E)(cacheValue == null ? null : cacheValue.enrichable);

      return e;
    }
  }

  public static class CacheValue {
    private final EnrichmentState state;
    private final Object enrichable;

    public CacheValue(EnrichmentState state, Object enrichable) {
      this.state = state;
      this.enrichable = enrichable;
    }
  }

  private enum PendingEnrichmentAction {ENQUEUE, FAIL, ENRICHED}

  private class PendingEnrichment {
    private final Enricher<K, ?> enricher;
    private final Class<?> enrichableClass;

    public PendingEnrichment(Enricher<K, ?> enricher, Class<?> enrichableClass) {
      assert enricher != null;
      assert enrichableClass != null;

      this.enricher = enricher;
      this.enrichableClass = enrichableClass;
    }

    /**
     * Returns <code>true</code> if this PendingEnrich was handled, otherwise <code>false</code>
     *
     * @param key a key
     * @return <code>true</code> if this PendingEnrich was handled, otherwise <code>false</code>
     */
    public PendingEnrichmentAction enrichIfConditionsMet(K key) {
      Map<Class<?>, Object> inputParameters = null;

      for(Class<?> parameterType : enricher.getInputTypes()) {
        CacheValue cacheValue = getDescendantCacheValue(key, parameterType);

        if(cacheValue == null) {
          enrich(parameterType, key);
          return PendingEnrichmentAction.ENQUEUE;
        }

        if(cacheValue.state == EnrichmentState.ENRICHMENT_FAILED) {
          return PendingEnrichmentAction.FAIL;
        }

        if(inputParameters == null) {
          inputParameters = new HashMap<>();
        }

        inputParameters.put(parameterType, cacheValue.enrichable);
      }

      enrichmentHandler.enrich(enricher.enrich(key, inputParameters), false);

      return PendingEnrichmentAction.ENRICHED;
    }

    public Class<?> getEnrichableClass() {
      return enrichableClass;
    }

    @Override
    public int hashCode() {
      return enricher.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null || getClass() != obj.getClass()) {
        return false;
      }

      @SuppressWarnings("unchecked")
      PendingEnrichment other = (PendingEnrichment)obj;

      return enricher.equals(other.enricher);
    }
  }
}
