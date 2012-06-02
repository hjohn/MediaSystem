package hs.mediasystem.enrich;

import hs.mediasystem.util.OrderedExecutionQueue;
import hs.mediasystem.util.OrderedExecutionQueueExecutor;
import hs.mediasystem.util.TaskExecutor;
import hs.mediasystem.util.TaskThreadPoolExecutor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class EnrichCache {
  private final Comparator<CacheKey> taskPriorityComparator = new Comparator<CacheKey>() {
    @Override
    public int compare(CacheKey o1, CacheKey o2) {
      if(o1.getPriority() > o2.getPriority()) {
        return -1;
      }
      else if(o1.getPriority() < o2.getPriority()) {
        return 1;
      }

      return 0;
    }
  };

  private final Map<Class<?>, Enricher<?>> ENRICHERS = new HashMap<>();

  private final Map<CacheKey, Map<Class<?>, CacheValue>> cache = new WeakHashMap<>();
  private final Map<CacheKey, Set<EnrichmentListener>> enrichmentListeners = new WeakHashMap<>();
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
    enrich(key, enrichableClass, false);
  }

  private <T> void enrich(CacheKey key, Class<T> enrichableClass, boolean bypassCache) {
    synchronized(cache) {
      @SuppressWarnings("unchecked")
      Enricher<T> enricher = (Enricher<T>)ENRICHERS.get(enrichableClass);

      if(enricher == null) {
        System.out.println("No suitable Enricher for " + enrichableClass);
        insertFailedEnrichment(key, enrichableClass);
      }
      else {
        PendingEnrichment<T> pendingEnrichment = new PendingEnrichment<>(enricher, enrichableClass, bypassCache);

        /*
         * First check if this Enrichment is not already pending.  Although the system has no problem handling
         * multiple the same Pending Enrichments (as after the first one is being processed the others only
         * result in a promotion) it can be wasteful to have many of these queued up.
         */

        Set<PendingEnrichment<?>> pendingEnrichments = pendingEnrichmentMap.get(key);

        if(pendingEnrichments != null && pendingEnrichments.contains(pendingEnrichment)) {

          /*
           * The Pending Enrichment already exists, promote the associated key (requeueing any tasks that might
           * use it) and then discard it.
           */

          List<Task<?>> cacheTasks = cacheQueue.removeAll(key);
          List<Task<?>> normalTasks = normalQueue.removeAll(key);

          key.promote();

          if(cacheTasks != null) {
            cacheQueue.submitAll(key, cacheTasks);
          }

          if(normalTasks != null) {
            normalQueue.submitAll(key, cacheTasks);
          }

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

  public void reload(CacheKey key) {
    synchronized(cache) {

      /*
       * Remove any queued tasks and any pending enrichments associated with this key,
       * cancel any pending enrichments to prevent them from adding old results to the
       * cache and gather a list of classes there were in the cache and were being
       * loaded.
       */

      cacheQueue.removeAll(key);
      normalQueue.removeAll(key);

      Set<Class<?>> classesToReload = new HashSet<>();
      Set<PendingEnrichment<?>> pendingEnrichments = pendingEnrichmentMap.remove(key);

      if(pendingEnrichments != null) {
        for(PendingEnrichment<?> pendingEnrichment : pendingEnrichments) {
          pendingEnrichment.cancel();
          classesToReload.add(pendingEnrichment.enrichableClass);
        }
      }

      Map<Class<?>, CacheValue> cacheValues = cache.get(key);

      if(cacheValues != null) {
        for(Iterator<Entry<Class<?>, CacheValue>> iterator = cacheValues.entrySet().iterator(); iterator.hasNext();) {
          Map.Entry<Class<?>, CacheValue> entry = iterator.next();

          if(entry.getValue().state != EnrichmentState.IMMUTABLE) {
            classesToReload.add(entry.getKey());
            iterator.remove();
          }
        }
      }

      /*
       * Re-enrich the classes we found, bypassing the cache
       */

      for(Class<?> classToReload : classesToReload) {
        enrich(key, classToReload, true);
      }
    }
  }

  private void insertFailedEnrichment(CacheKey key, Class<?> enrichableClass) {
    insertInternal(key, EnrichmentState.FAILED, enrichableClass, null, false);
  }

  @SuppressWarnings("unchecked")
  public <E> void insertImmutableDataIfNotExists(CacheKey key, E enrichable) {
    if(enrichable == null) {
      throw new IllegalArgumentException("parameter 'enrichable' cannot be null");
    }
    insertInternal(key, EnrichmentState.IMMUTABLE, (Class<E>)enrichable.getClass(), enrichable, true);
  }

  @SuppressWarnings("unchecked")
  public <E> void insert(CacheKey key, E enrichable) {
    if(enrichable == null) {
      throw new IllegalArgumentException("parameter 'enrichable' cannot be null");
    }
    insertInternal(key, EnrichmentState.ENRICHED, (Class<E>)enrichable.getClass(), enrichable, false);
  }

  @SuppressWarnings("unchecked")
  public <E> void insertImmutable(CacheKey key, E enrichable) {
    if(enrichable == null) {
      throw new IllegalArgumentException("parameter 'enrichable' cannot be null");
    }
    insertInternal(key, EnrichmentState.IMMUTABLE, (Class<E>)enrichable.getClass(), enrichable, false);
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
    private final boolean bypassCache;

    private PendingEnrichmentState state = PendingEnrichmentState.NOT_INITIALIZED;
    private volatile boolean cancelled;

    public PendingEnrichment(Enricher<T> enricher, Class<T> enrichableClass, boolean bypassCache) {
      assert enricher != null;
      assert enrichableClass != null;

      this.enricher = enricher;
      this.enrichableClass = enrichableClass;
      this.bypassCache = bypassCache;
    }

    public void cancel() {
      cancelled = true;
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
      synchronized(cache) {
        if(state == PendingEnrichmentState.IN_PROGRESS || state == PendingEnrichmentState.BROKEN_DEPENDENCY) {
          throw new IllegalStateException("" + state);
        }

        if(cancelled) {
          return;
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
            synchronized(cache) {
              if(cancelled) {
                return;
              }

              List<EnrichTask<T>> enrichTasks = enricher.enrich(finalInputParameters, bypassCache);

              for(int i = 0; i < enrichTasks.size(); i++) {
                final EnrichTask<T> enrichTask = enrichTasks.get(i);
                final EnrichTask<T> nextEnrichTask = (i < enrichTasks.size() - 1) ? enrichTasks.get(i + 1) : null;

                enrichTask.stateProperty().addListener(new ChangeListener<State>() {
                  @Override
                  public void changed(ObservableValue<? extends State> observable, State oldState, State state) {
                    synchronized(cache) {
                      if(!cancelled && (state == State.FAILED || state == State.CANCELLED || state == State.SUCCEEDED)) {
                        T taskResult = enrichTask.getValue();

                        System.out.println("[" + (state == State.FAILED ? "WARN" : "FINE") + "] EnrichCache [" + enricher.getClass().getSimpleName() + "->" + enrichableClass.getSimpleName() + "]: " + state + ": " + key + ": " + enrichTask.getTitle() + " -> " + taskResult);

                        if(state == State.FAILED) {
                          System.out.println("[WARN] EnrichCache [" + enricher.getClass().getSimpleName() + "->" + enrichableClass.getSimpleName() + "]: " + state + ": " + key + ": " + enrichTask.getTitle() + ": " + enrichTask.getException());
                          enrichTask.getException().printStackTrace(System.out);
                          insertFailedEnrichment(key, enrichableClass);
                        }

                        if(state == State.SUCCEEDED) {
                          if(taskResult != null || nextEnrichTask == null) {
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
                          else {
                            System.out.println("[FINE] EnrichCache [" + enricher.getClass().getSimpleName() + "->" + enrichableClass.getSimpleName() + "]: ENRICH_NEXT: " + key + ": " + nextEnrichTask.getTitle());

                            if(nextEnrichTask.isFast()) {
                              cacheQueue.submit(key, nextEnrichTask);
                            }
                            else {
                              normalQueue.submit(key, nextEnrichTask);
                            }
                          }
                        }

                        cacheQueue.submitPending();
                        normalQueue.submitPending();
                      }
                    }
                  }
                });

              }

              EnrichTask<T> firstTask = enrichTasks.get(0);

              System.out.println("[FINE] EnrichCache [" + enricher.getClass().getSimpleName() + "->" + enrichableClass.getSimpleName() + "]: ENRICH: " + key + ": " + firstTask.getTitle());

              if(firstTask.isFast()) {
                cacheQueue.submit(key, firstTask);
              }
              else {
                normalQueue.submit(key, firstTask);
              }
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
