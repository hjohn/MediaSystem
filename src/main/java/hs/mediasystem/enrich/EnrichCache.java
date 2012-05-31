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
public class EnrichCache {
  private final Comparator<CacheKey> taskPriorityComparator = new Comparator<CacheKey>() {
    @Override
    public int compare(CacheKey o1, CacheKey o2) {
      return 0;
    }
  };

  private final Map<Class<?>, Enricher<?>> ENRICHERS = new HashMap<>();

  private final Map<CacheKey, Map<Class<?>, CacheValue>> cache = new WeakHashMap<>();
  private final Map<CacheKey, Set<EnrichmentListener>> enrichmentListeners = new WeakHashMap<>();  // TODO EnrichmentListener refs must be weak
  private final Map<CacheKey, Set<PendingEnrichment<?>>> pendingEnrichmentMap = new WeakHashMap<>();

  private final OrderedExecutionQueue<CacheKey> cacheQueue = new OrderedExecutionQueueExecutor<>(taskPriorityComparator, new TaskThreadPoolExecutor(new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())));
  private final OrderedExecutionQueue<CacheKey> normalQueue;

  public <T> void registerEnricher(Class<T> cls, Enricher<T> enricher) {
    ENRICHERS.put(cls, enricher);
  }

  @Inject
  public EnrichCache(TaskExecutor normalExecutor) {
    this.normalQueue = new OrderedExecutionQueueExecutor<>(taskPriorityComparator, normalExecutor);
  }

  public void addListener(CacheKey key, EnrichmentListener listener) {
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

  public void removeListener(CacheKey key, EnrichmentListener listener) {
    synchronized(cache) {
      Set<EnrichmentListener> listeners = enrichmentListeners.get(key);

      if(listeners != null) {
        listeners.remove(listener);
      }
    }
  }

  public CacheValue getCacheValue(CacheKey key, Class<?> enrichableClass) {
    synchronized(cache) {
      Map<Class<?>, CacheValue> cacheValues = cache.get(key);

      if(cacheValues == null) {
        return null;
      }

      return cacheValues.get(enrichableClass);
    }
  }

  public CacheValue getDescendantCacheValue(CacheKey key, Class<?> enrichableClass) {
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

  public <T> void enrich(CacheKey key, Class<T> enrichableClass) {
    // System.out.println("[FINE] Enrichers.enrich() - " + enrichableClass.getName() + ": " + key);

    synchronized(cache) {
      @SuppressWarnings("unchecked")
      Enricher<T> enricher = (Enricher<T>)ENRICHERS.get(enrichableClass);

      if(enricher == null) {
        System.out.println("No suitable Enricher for " + enrichableClass);
        new Exception().printStackTrace();
        insertFailedEnrichment(key, enrichableClass);
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
          insertFailedEnrichment(key, pendingEnrichment.getEnrichableClass());
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

  private void insertFailedEnrichment(CacheKey key, Class<?> enrichableClass) {
    insertInternal(key, EnrichmentState.FAILED, enrichableClass, null, false);
  }

  @SuppressWarnings("unchecked")
  public <E> void insertUnenrichedDataIfNotExists(CacheKey key, E enrichable) {
    if(enrichable == null) {
      throw new IllegalArgumentException("parameter 'enrichable' cannot be null");
    }
    insertInternal(key, EnrichmentState.UNENRICHED, (Class<E>)enrichable.getClass(), enrichable, true);
  }

  @SuppressWarnings("unchecked")
  public <E> void insert(CacheKey key, E enrichable) {
    if(enrichable == null) {
      throw new IllegalArgumentException("parameter 'enrichable' cannot be null");
    }
    insertInternal(key, EnrichmentState.ENRICHED, (Class<E>)enrichable.getClass(), enrichable, false);
  }

  private <E> void insertInternal(CacheKey key, EnrichmentState state, Class<E> enrichableClass, E enrichable, boolean onlyIfNotExists) {
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
              insertFailedEnrichment(key, pendingEnrichment.getEnrichableClass());

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

  public <E> E getFromCache(CacheKey key, Class<E> enrichableClass) {
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
    private final Enricher<T> enricher;
    private final Class<T> enrichableClass;

    private PendingEnrichmentState state = PendingEnrichmentState.NOT_INITIALIZED;

    public PendingEnrichment(Enricher<T> enricher, Class<T> enrichableClass) {
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
    public void enrichIfConditionsMet(final CacheKey key) {
      synchronized(this) {
        if(state == PendingEnrichmentState.IN_PROGRESS || state == PendingEnrichmentState.BROKEN_DEPENDENCY) {
          throw new IllegalStateException("" + state);
        }

        Map<Class<?>, Object> inputParameters = null;

        for(Class<?> parameterType : enricher.getInputTypes()) {
          CacheValue cacheValue = getDescendantCacheValue(key, parameterType);

          if(cacheValue == null) {
            state = PendingEnrichmentState.WAITING_FOR_DEPENDENCY;
            enrich(key, parameterType);
            return;
          }

          if(cacheValue.state == EnrichmentState.FAILED) {
            state = PendingEnrichmentState.BROKEN_DEPENDENCY;
            return;
          }

          if(inputParameters == null) {
            inputParameters = new HashMap<>();
          }

          inputParameters.put(parameterType, cacheValue.enrichable);
        }

        state = PendingEnrichmentState.IN_PROGRESS;

        final Parameters finalInputParameters = new Parameters(inputParameters);

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            List<EnrichTask<T>> enrichTasks = enricher.enrich(finalInputParameters, false);

            for(int i = 0; i < enrichTasks.size(); i++) {
              final EnrichTask<T> enrichTask = enrichTasks.get(i);
              final EnrichTask<T> nextEnrichTask = (i < enrichTasks.size() - 1) ? enrichTasks.get(i + 1) : null;

              enrichTask.stateProperty().addListener(new ChangeListener<State>() {
                @Override
                public void changed(ObservableValue<? extends State> observable, State oldState, State state) {
                  if(state == State.FAILED || state == State.CANCELLED || state == State.SUCCEEDED) {
                    T taskResult = enrichTask.getValue();

                    if(state == State.SUCCEEDED || state == State.CANCELLED) {
                      System.out.println("[FINE] EnrichCache: " + state + ": " + key + ": " + enrichTask.getTitle());
                    }

                    if(state == State.FAILED) {
                      System.out.println("[WARN] EnrichCache: " + state + ": " + key + ": " + enrichTask.getTitle() + ": " + enrichTask.getException());
                      enrichTask.getException().printStackTrace(System.out);
                      insertFailedEnrichment(key, enrichableClass);
                    }

                    if(state == State.SUCCEEDED) {
                      if(taskResult != null || nextEnrichTask == null) {
                        synchronized(cache) {
                          if(taskResult != null) {
                            insert(key, taskResult);
                          }
                          else {
                            insertFailedEnrichment(key, enrichableClass);
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

      PendingEnrichment<?> other = (PendingEnrichment<?>)obj;

      return enricher.equals(other.enricher);
    }
  }
}
