package hs.mediasystem.enrich;

import hs.mediasystem.util.OrderedExecutionQueue;
import hs.mediasystem.util.OrderedExecutionQueueExecutor;
import hs.mediasystem.util.TaskExecutor;
import hs.mediasystem.util.TaskThreadPoolExecutor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EnrichCache<K> {
  private final Comparator<K> taskPriorityComparator = new Comparator<K>() {
    @Override
    public int compare(K o1, K o2) {
      return 0;
    }
  };

  private final Map<Class<?>, Enricher<K, ?>> ENRICHERS = new HashMap<>();

  private final Map<K, Map<Class<?>, CacheValue>> cache = new WeakHashMap<>();
  private final Map<K, Set<EnrichmentListener>> enrichmentListeners = new WeakHashMap<>();  // TODO EnrichmentListener refs must be weak
  private final Map<K, Set<PendingEnrichment<?>>> pendingEnrichmentMap = new WeakHashMap<>();

  private final OrderedExecutionQueue<K> cacheQueue = new OrderedExecutionQueueExecutor<>(taskPriorityComparator, new TaskThreadPoolExecutor(new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())));
  private final OrderedExecutionQueue<K> normalQueue;

  public <T> void registerEnricher(Class<T> cls, Enricher<K, T> enricher) {
    ENRICHERS.put(cls, enricher);
  }

  @Inject
  public EnrichCache(TaskExecutor normalExecutor) {
    this.normalQueue = new OrderedExecutionQueueExecutor<>(taskPriorityComparator, normalExecutor);
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
        PendingEnrichment<T> pendingEnrichment = new PendingEnrichment<>(enricher, enrichableClass);

        /*
         * First check if this Enrichment is not already pending.  Although the system has no problem handling
         * multiple the same Pending Enrichments (as after the first one is being processed the others only
         * result in a promotion) it can be wasteful to have many of these queued up.
         */

        Set<PendingEnrichment<?>> pendingEnrichments = pendingEnrichmentMap.get(key);

        if(pendingEnrichments != null && pendingEnrichments.contains(pendingEnrichment)) {

          /*
           * The Pending Enrichment already exists, just discard.
           */

          return;
        }

        /*
         * If this unique Enrichment fails immediately, we donot bother adding it to the PENDING_ENRICHMENTS map.
         */

        pendingEnrichment.enrichIfConditionsMet(key);

        if(pendingEnrichment.getState() == PendingEnrichmentState.BROKEN_DEPENDENCY) {
          insert(key, EnrichmentState.ENRICHMENT_FAILED, pendingEnrichment.getEnrichableClass(), null);
        }
        else {
          pendingEnrichments = pendingEnrichmentMap.get(key);  // get Set again as it can be non-null now after calling enrichIfConditionsMet above

          if(pendingEnrichments == null) {
            pendingEnrichments = new HashSet<>();
            pendingEnrichmentMap.put(key, pendingEnrichments);
          }

          pendingEnrichments.add(pendingEnrichment);
        }
      }
    }
  }

  public void insertIfNotExists(K key, EnrichmentState state, Class<?> enrichableClass, Object enrichable) {
    insertInternal(key, state, enrichableClass, enrichable, true);
  }

  public void insert(K key, EnrichmentState state, Class<?> enrichableClass, Object enrichable) {
    insertInternal(key, state, enrichableClass, enrichable, false);
  }

  private void insertInternal(K key, EnrichmentState state, Class<?> enrichableClass, Object enrichable, boolean onlyIfNotExists) {
    synchronized(cache) {
      Map<Class<?>, CacheValue> cacheValues = cache.get(key);

      if(cacheValues == null) {
        cacheValues = new HashMap<>();
        cache.put(key, cacheValues);
      }

      if(onlyIfNotExists && cacheValues.containsKey(enrichableClass)) {
        return;
      }

      cacheValues.put(enrichableClass, new CacheValue(state, enrichable));

      Set<EnrichmentListener> listeners = enrichmentListeners.get(key);

      if(listeners != null) {
        for(EnrichmentListener listener : new HashSet<>(listeners)) {
          listener.update(state, enrichableClass, enrichable);
        }
      }

      /*
       * Notify any waiting PendingEnrichments of the newly inserted data
       */

      Set<PendingEnrichment<?>> pendingEnrichments = pendingEnrichmentMap.get(key);

      if(pendingEnrichments != null) {
        for(PendingEnrichment<?> pendingEnrichment : new HashSet<>(pendingEnrichments)) {
          if(pendingEnrichment.getState() == PendingEnrichmentState.WAITING_FOR_DEPENDENCY) {
            pendingEnrichment.enrichIfConditionsMet(key);

            if(pendingEnrichment.getState() == PendingEnrichmentState.BROKEN_DEPENDENCY) {
              insert(key, EnrichmentState.ENRICHMENT_FAILED, pendingEnrichment.getEnrichableClass(), null);

              pendingEnrichments.remove(pendingEnrichment);

              if(pendingEnrichments.isEmpty()) {
                pendingEnrichmentMap.remove(key);
              }
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

  private enum PendingEnrichmentState {NOT_INITIALIZED, WAITING_FOR_DEPENDENCY, BROKEN_DEPENDENCY, IN_PROGRESS}

  private class PendingEnrichment<T> {
    private final Enricher<K, T> enricher;
    private final Class<?> enrichableClass;

    private PendingEnrichmentState state = PendingEnrichmentState.NOT_INITIALIZED;

    public PendingEnrichment(Enricher<K, T> enricher, Class<T> enrichableClass) {
      assert enricher != null;
      assert enrichableClass != null;

      this.enricher = enricher;
      this.enrichableClass = enrichableClass;
    }

    public PendingEnrichmentState getState() {
      return state;
    }

    /**
     * Returns <code>true</code> if this PendingEnrich was handled, otherwise <code>false</code>
     *
     * @param key a key
     * @return <code>true</code> if this PendingEnrich was handled, otherwise <code>false</code>
     */
    public void enrichIfConditionsMet(final K key) {
      synchronized(this) {
        if(state == PendingEnrichmentState.IN_PROGRESS || state == PendingEnrichmentState.BROKEN_DEPENDENCY) {
          throw new IllegalStateException("" + state);
        }

        Map<Class<?>, Object> inputParameters = null;

        for(Class<?> parameterType : enricher.getInputTypes()) {
          CacheValue cacheValue = getDescendantCacheValue(key, parameterType);

          if(cacheValue == null) {
            state = PendingEnrichmentState.WAITING_FOR_DEPENDENCY;
            enrich(parameterType, key);
            return;
          }

          if(cacheValue.state == EnrichmentState.ENRICHMENT_FAILED) {
            state = PendingEnrichmentState.BROKEN_DEPENDENCY;
            return;
          }

          if(inputParameters == null) {
            inputParameters = new HashMap<>();
          }

          inputParameters.put(parameterType, cacheValue.enrichable);
        }

        state = PendingEnrichmentState.IN_PROGRESS;

        final Map<Class<?>, Object> finalInputParameters = inputParameters;

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            List<EnrichTask<T>> enrichTasks = enricher.enrich(key, finalInputParameters, false);

            for(int i = 0; i < enrichTasks.size(); i++) {
              final EnrichTask<T> enrichTask = enrichTasks.get(i);
              final EnrichTask<T> nextEnrichTask = (i < enrichTasks.size() - 1) ? enrichTasks.get(i + 1) : null;

              enrichTask.stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue<? extends State> observable, State oldState, State state) {
                  if(state == State.FAILED || state == State.CANCELLED || state == State.SUCCEEDED) {
                    EnrichmentResult<T> taskResult = enrichTask.getValue();

                    if(state == State.SUCCEEDED || state == State.CANCELLED) {
                      System.out.println("[FINE] EnrichCache: " + state + ": " + key + ": " + enrichTask.getTitle());
                    }

                    if(state == State.FAILED) {
                      System.out.println("[WARN] EnrichCache: " + state + ": " + key + ": " + enrichTask.getTitle() + ": " + enrichTask.getException());
                      enrichTask.getException().printStackTrace(System.out);
                      insert(key, EnrichmentState.ENRICHMENT_FAILED, enrichableClass, null);
                    }

                    if(state == State.SUCCEEDED) {
                      if(taskResult != null && taskResult.getPrimaryResult() == null) {
                        for(Object o : taskResult.getIntermediateResults()) {
                          if(o != null) {
                            insert(key, EnrichmentState.ENRICHED, o.getClass(), o);
                          }
                        }

                        taskResult = null;
                      }

                      if((taskResult != null && taskResult.getPrimaryResult() != null) || nextEnrichTask == null) {
                        synchronized(cache) {
                          if(taskResult != null) {
                            insert(key, EnrichmentState.ENRICHED, enrichableClass, taskResult.getPrimaryResult());
                          }
                          else {
                            insert(key, EnrichmentState.ENRICHMENT_FAILED, enrichableClass, null);
                          }

                          Set<PendingEnrichment<?>> set = pendingEnrichmentMap.get(key);

                          set.remove(PendingEnrichment.this);

                          if(set.isEmpty()) {
                            pendingEnrichmentMap.remove(key);
                          }
                        }
                      }
                      else if(nextEnrichTask.isFast()) {
                        cacheQueue.submit(key, nextEnrichTask);
                      }
                      else {
                        normalQueue.submit(key, nextEnrichTask);
                      }
                    }

                    cacheQueue.submitPending();
                    normalQueue.submitPending();
                  }
                }
              });
            }

            EnrichTask<T> firstTask = enrichTasks.get(0);

            if(firstTask.isFast()) {
              cacheQueue.submit(key, firstTask);
            }
            else {
              normalQueue.submit(key, firstTask);
            }
          }
        });
      }
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
      PendingEnrichment<?> other = (PendingEnrichment<?>)obj;

      return enricher.equals(other.enricher);
    }
  }
}
