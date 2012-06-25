package hs.mediasystem.enrich;

import hs.mediasystem.util.OrderedExecutionQueue;
import hs.mediasystem.util.OrderedExecutionQueueExecutor;
import hs.mediasystem.util.TaskExecutor;
import hs.mediasystem.util.TaskThreadPoolExecutor;
import hs.mediasystem.util.UniqueArrayList;
import hs.mediasystem.util.UniqueList;
import hs.mediasystem.util.WeakValueMap;

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
import java.util.concurrent.atomic.AtomicLong;

import javafx.concurrent.Task;

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

  private final WeakValueMap<String, CacheKey> cacheKeys = new WeakValueMap<>();
  private final Map<CacheKey, Map<Class<?>, CacheValue>> cache = new WeakHashMap<>();
  private final Map<CacheKey, Set<EnrichmentListener>> enrichmentListeners = new WeakHashMap<>();
  private final Map<CacheKey, UniqueList<PendingEnrichment<?>>> pendingEnrichmentMap = new WeakHashMap<>();  // PendingEnrichments are in reverse execution order

  private final OrderedExecutionQueue<CacheKey> cacheQueue = new OrderedExecutionQueueExecutor<>(taskPriorityComparator, new TaskThreadPoolExecutor(new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>())));
  private final OrderedExecutionQueue<CacheKey> normalQueue;

  public <T> void registerEnricher(Class<T> cls, Enricher<T> enricher) {
    ENRICHERS.put(cls, enricher);
  }

  @Inject
  public EnrichCache(TaskExecutor normalExecutor) {
    this.normalQueue = new OrderedExecutionQueueExecutor<>(taskPriorityComparator, normalExecutor);
  }

  public CacheKey obtainKey(String id) {
    synchronized(cacheKeys) {
      CacheKey key = cacheKeys.get(id);

      if(key == null) {
        key = new CacheKey(id);
        cacheKeys.put(id, key);
      }

      return key;
    }
  }

  public void addListener(CacheKey key, EnrichmentListener listener) {
    if(key == null) {
      throw new IllegalArgumentException("parameter 'key' cannot be null");
    }

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
          if(entry.getValue().state == EnrichmentState.ENRICHED) {
            listener.update(entry.getValue().state, entry.getKey(), entry.getValue().enrichable);
          }
        }
      }
    }
  }

  public void removeListener(CacheKey key, EnrichmentListener listener) {
    if(key == null) {
      throw new IllegalArgumentException("parameter 'key' cannot be null");
    }

    synchronized(cache) {
      Set<EnrichmentListener> listeners = enrichmentListeners.get(key);

      if(listeners != null) {
        listeners.remove(listener);
      }
    }
  }

  public CacheValue getCacheValue(CacheKey key, Class<?> enrichableClass) {
    if(key == null) {
      throw new IllegalArgumentException("parameter 'key' cannot be null");
    }

    synchronized(cache) {
      Map<Class<?>, CacheValue> cacheValues = cache.get(key);

      if(cacheValues == null) {
        return null;
      }

      return cacheValues.get(enrichableClass);
    }
  }

  public CacheValue getDescendantCacheValue(CacheKey key, Class<?> enrichableClass) {
    if(key == null) {
      throw new IllegalArgumentException("parameter 'key' cannot be null");
    }

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
    if(key == null) {
      throw new IllegalArgumentException("parameter 'key' cannot be null");
    }

    synchronized(cache) {
      PendingEnrichment<T> pendingEnrichment = createPendingEnrichment(key, enrichableClass, bypassCache);

      if(pendingEnrichment == null) {
        System.out.println("[FINE] EnrichCache: No suitable Enricher for " + enrichableClass);
        insertFailedEnrichment(key, enrichableClass);
      }
      else {

        /*
         * First check if this Enrichment is not already pending.  Although the system has no problem handling
         * multiple the same Pending Enrichments (as after the first one is being processed the others only
         * result in a promotion) it can be wasteful to have many of these queued up.
         */

        UniqueList<PendingEnrichment<?>> pendingEnrichments = pendingEnrichmentMap.get(key);

        if(pendingEnrichments != null && pendingEnrichments.contains(pendingEnrichment)) {

          /*
           * The Pending Enrichment already exists, just discard it and promote the associated key (requeueing
           * any tasks that might use it).
           */

          List<Task<?>> cacheTasks = cacheQueue.removeAll(key);
          List<Task<?>> normalTasks = normalQueue.removeAll(key);

          key.promote();

          if(cacheTasks != null) {
            cacheQueue.submitAll(key, cacheTasks);
          }

          if(normalTasks != null) {
            normalQueue.submitAll(key, normalTasks);
          }

          return;
        }

        UniqueList<PendingEnrichment<?>> dependencies = pendingEnrichment.initialize();

        if(dependencies != null) {
          System.out.println("[FINE] EnrichCache [" + pendingEnrichment.enricher.getClass().getSimpleName() + "->" + enrichableClass.getSimpleName() + "]: TRIGGER: " + key);

          if(pendingEnrichments == null) {
            pendingEnrichments = new UniqueArrayList<>();
            pendingEnrichmentMap.put(pendingEnrichment.key, pendingEnrichments);
          }

          pendingEnrichments.add(pendingEnrichment);
          pendingEnrichments.removeAll(dependencies);  // required so any pre-existing dependencies are positioned AFTER this pending enrichment (and so are processed before it)
          pendingEnrichments.addAll(dependencies);
          queueNext(key);
        }
        else {
          System.out.println("[FINE] EnrichCache [" + pendingEnrichment.enricher.getClass().getSimpleName() + "->" + enrichableClass.getSimpleName() + "]: TRIGGER_FAILED: " + key);
        }
      }
    }
  }

  private <T> PendingEnrichment<T> createPendingEnrichment(CacheKey key, Class<T> enrichableClass, boolean bypassCache) {
    @SuppressWarnings("unchecked")
    Enricher<T> enricher = (Enricher<T>)ENRICHERS.get(enrichableClass);

    return enricher == null ? null : new PendingEnrichment<>(key, enricher, enrichableClass, bypassCache);
  }

  public void reload(CacheKey key) {
    if(key == null) {
      throw new IllegalArgumentException("parameter 'key' cannot be null");
    }

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
    if(key == null) {
      throw new IllegalArgumentException("parameter 'key' cannot be null");
    }

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

      if(state != EnrichmentState.IMMUTABLE) {
        Set<EnrichmentListener> listeners = enrichmentListeners.get(key);

        if(listeners != null) {
          for(EnrichmentListener listener : new HashSet<>(listeners)) {
            listener.update(state, enrichableClass, enrichable);
          }
        }
      }

      /*
       * Notify any waiting PendingEnrichments of the newly inserted data
       */

      queueNext(key);
    }
  }

  public <E> E getFromCache(CacheKey key, Class<E> enrichableClass) {
    if(key == null) {
      throw new IllegalArgumentException("parameter 'key' cannot be null");
    }

    synchronized(cache) {
      CacheValue cacheValue = getCacheValue(key, enrichableClass);

      @SuppressWarnings("unchecked")
      E e = (E)(cacheValue == null ? null : cacheValue.enrichable);

      return e;
    }
  }

  private void removePendingEnrichment(PendingEnrichment<?> pendingEnrichment) {
    synchronized(cache) {
      Set<PendingEnrichment<?>> pendingEnrichments = pendingEnrichmentMap.get(pendingEnrichment.key);

      assert pendingEnrichments != null;

      pendingEnrichments.remove(pendingEnrichment);

      if(pendingEnrichments.isEmpty()) {
        pendingEnrichmentMap.remove(pendingEnrichment.key);
      }
    }
  }

  private PendingEnrichment<?> getNextPendingEnrichment(CacheKey key) {
    UniqueList<PendingEnrichment<?>> pendingEnrichments = pendingEnrichmentMap.get(key);

    if(pendingEnrichments == null) {
      return null;
    }

    return pendingEnrichments.get(pendingEnrichments.size() - 1);
  }

  /**
   * Queues any available task for the given key.  Only allowed to be called when there is no PendingEnrichment in ACTIVE state.
   *
   * @param key a cache key
   */
  private void queueNext(CacheKey key) {
    synchronized(cache) {
      PendingEnrichment<?> pendingEnrichment;

      while((pendingEnrichment = getNextPendingEnrichment(key)) != null && pendingEnrichment.getState() != PendingEnrichmentState.ACTIVE) {
        assert pendingEnrichment.getState() == PendingEnrichmentState.INITIALIZED;

        EnrichCacheTask task = pendingEnrichment.enrich();

        if(task != null) {
          EnrichCacheTask activeTask = key.getActiveTask();

          if(activeTask != null && activeTask.isFast() && !task.isFast()) {
            activeTask = null;
          }

          if(activeTask != null) {
            activeTask.setNextTask(task);
          }
          else {
            key.setActiveTask(task);

            if(task.isFast()) {
              cacheQueue.submit(key, task);
            }
            else {
              normalQueue.submit(key, task);
            }
          }

          return;
        }
      }

      key.clearActiveTask();
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

  private enum PendingEnrichmentState {

    /**
     * Initial state, before the dependencies have been calculated.
     */
    UNINITIALIZED,  // -> INITIALIZED, FAILED

    /**
     * Dependencies have been calculated.
     */
    INITIALIZED,  // -> FAILED, ACTIVE, CANCELLED

    /**
     * Enrichment is ready to be processed.
     */
    ACTIVE,  // -> INITIALIZED, CANCELLED

    /**
     * End state indicating the enrichment was cancelled.
     */
    CANCELLED
  }

  private class PendingEnrichment<T> {
    private final CacheKey key;
    private final Enricher<T> enricher;
    private final Class<T> enrichableClass;
    private final boolean bypassCache;

    private List<EnrichTask<T>> enrichTasks;

    private volatile PendingEnrichmentState state = PendingEnrichmentState.UNINITIALIZED;

    public PendingEnrichment(final CacheKey key, Enricher<T> enricher, Class<T> enrichableClass, boolean bypassCache) {
      assert key != null;
      assert enricher != null;
      assert enrichableClass != null;

      this.key = key;
      this.enricher = enricher;
      this.enrichableClass = enrichableClass;
      this.bypassCache = bypassCache;
    }

    public void cancel() {
      assert state == PendingEnrichmentState.INITIALIZED || state == PendingEnrichmentState.ACTIVE;

      state = PendingEnrichmentState.CANCELLED;
    }

    public boolean isCancelled() {
      return state == PendingEnrichmentState.CANCELLED;
    }

    public void requeue() {
      assert state == PendingEnrichmentState.ACTIVE;

      state = PendingEnrichmentState.INITIALIZED;
    }

    public PendingEnrichmentState getState() {
      return state;
    }

    /**
     * Returns either a List of dependencies that still need to be fulfilled (INITIALIZED) or <code>null</code> if
     * fulfilling them is impossible.  If an empty list is returned it means all dependencies are
     * fulfilled and this PendingEnrichment is ready to be fulfilled (INITIALIZED).
     *
     * @return a List of dependencies that still need to be fulfilled or <code>null</code>
     */
    public UniqueList<PendingEnrichment<?>> initialize() {
      synchronized(cache) {
        assert state == PendingEnrichmentState.UNINITIALIZED;

        UniqueList<PendingEnrichment<?>> list = new UniqueArrayList<>();

        for(Class<?> parameterType : enricher.getInputTypes()) {
          CacheValue cacheValue = getDescendantCacheValue(key, parameterType);

          if(cacheValue == null) {
            PendingEnrichment<?> pendingEnrichment = createPendingEnrichment(key, parameterType, bypassCache);
            UniqueList<PendingEnrichment<?>> childDependencies = pendingEnrichment.initialize();

            if(childDependencies == null) {
              insertFailedEnrichment(key, enrichableClass);
              return null;
            }

            list.add(pendingEnrichment);
            list.addAll(childDependencies);
          }
          else if(cacheValue.state == EnrichmentState.FAILED) {
            insertFailedEnrichment(key, enrichableClass);
            return null;
          }
        }

        state = PendingEnrichmentState.INITIALIZED;

        return list;
      }
    }

    private Map<Class<?>, Object> getParameters() {
      Map<Class<?>, Object> inputParameters = new HashMap<>();

      for(Class<?> parameterType : enricher.getInputTypes()) {
        CacheValue cacheValue = getDescendantCacheValue(key, parameterType);

        assert cacheValue != null;

        if(cacheValue.state == EnrichmentState.FAILED) {
          return null;
        }

        inputParameters.put(parameterType, cacheValue.enrichable);
      }

      return inputParameters;
    }

    public EnrichCacheTask enrich() {
      synchronized(cache) {
        assert state == PendingEnrichmentState.INITIALIZED;

        if(enrichTasks == null) {
          Map<Class<?>, Object> inputParameters = getParameters();

          if(inputParameters != null) {
            enrichTasks = enricher.enrich(new Parameters(inputParameters), bypassCache);
          }
        }

        if(enrichTasks == null || enrichTasks.isEmpty()) {
          removePendingEnrichment(this);
          insertFailedEnrichment(key, enrichableClass);
          return null;
        }

        state = PendingEnrichmentState.ACTIVE;

        return new EnrichCacheTask(this, enrichTasks.remove(0));
      }
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

  public class EnrichCacheTask extends Task<Void> {
    private PendingEnrichment<?> pendingEnrichment;
    private EnrichTask<?> currentEnrichTask;
    private EnrichCacheTask nextEnrichCacheTask;

    public EnrichCacheTask(PendingEnrichment<?> pendingEnrichment, EnrichTask<?> enrichTask) {
      this.pendingEnrichment = pendingEnrichment;
      this.currentEnrichTask = enrichTask;

      enrichTask.setDelegationTask(this);
    }

    public void setNextTask(EnrichCacheTask task) {
      assert task != null;
      assert nextEnrichCacheTask == null;

      this.nextEnrichCacheTask = task;
    }

    public boolean isFast() {
      return currentEnrichTask.isFast();
    }

    @Override
    public void updateTitle(String title) {
      super.updateTitle(title);
    }

    @Override
    public void updateMessage(String message) {
      super.updateMessage(message);
    }

    @Override
    public void updateProgress(long workDone, long max) {
      super.updateProgress(workDone, max);
    }

    @Override
    protected final Void call() throws Exception {
      for(;;) {
        try {
          System.out.println("[FINE] " + createLogLine("RUNNING"));

          Object taskResult = currentEnrichTask.call();

          if(taskResult == null) {
            pendingEnrichment.requeue();  // requeue the original enrichment, which may have other options to get the needed information
            queueNext(pendingEnrichment.key);
          }
          else {
            finishPendingEnrichment(taskResult, null);
          }
        }
        catch(Exception e) {
          finishPendingEnrichment(null, e);
        }
        finally {
          cacheQueue.submitPending();
          normalQueue.submitPending();
        }

        if(nextEnrichCacheTask == null) {
          break;
        }

        currentEnrichTask = nextEnrichCacheTask.currentEnrichTask;
        pendingEnrichment = nextEnrichCacheTask.pendingEnrichment;
        nextEnrichCacheTask = null;

        currentEnrichTask.setDelegationTask(this);
      }

      return null;
    }

    private String createLogLine(String state) {
      return "EnrichCache [" + pendingEnrichment.enricher.getClass().getSimpleName() + "->" + pendingEnrichment.enrichableClass.getSimpleName() + "]: " + state + ": " + pendingEnrichment.key + ": " + currentEnrichTask.getTitle();
    }

    private void finishPendingEnrichment(Object taskResult, Exception e) {
      synchronized(cache) {
        if(!pendingEnrichment.isCancelled()) {
          removePendingEnrichment(pendingEnrichment);

          if(taskResult != null) {
            System.out.println("[FINE] " + createLogLine("COMPLETED") + " -> " + taskResult);
            insert(pendingEnrichment.key, taskResult);
          }
          else {
            System.out.println("[WARN] " + createLogLine("FAILED") + ": " + e);
            e.printStackTrace(System.out);
            insertFailedEnrichment(pendingEnrichment.key, pendingEnrichment.enrichableClass);
          }
        }
        else {
          System.out.println("[FINE] " + createLogLine("CANCELLED"));
        }
      }
    }
  }

  public static class CacheKey {
    private static final AtomicLong PRIORITY = new AtomicLong(0);

    private final String id;

    private EnrichCacheTask activeTask;
    private long priority;

    CacheKey(String id) {
      assert id != null;

      this.id = id;
      this.priority = PRIORITY.incrementAndGet();
    }

    public EnrichCacheTask getActiveTask() {
      return activeTask;
    }

    public void setActiveTask(EnrichCacheTask task) {
      assert task != null;

      activeTask = task;
    }

    public void clearActiveTask() {
      activeTask = null;
    }

    public long getPriority() {
      return priority;
    }

    public void promote() {
      this.priority = PRIORITY.incrementAndGet();
    }

    @Override
    public int hashCode() {
      return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if(this == obj) {
        return true;
      }
      if(obj == null || getClass() != obj.getClass()) {
        return false;
      }

      CacheKey other = (CacheKey)obj;

      return id.equals(other.id);
    }

    @Override
    public String toString() {
      return "CacheKey[" + id + "; p=" + priority + "]";
    }
  }
}
