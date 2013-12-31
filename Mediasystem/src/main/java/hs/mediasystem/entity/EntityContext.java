package hs.mediasystem.entity;

import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.persist.PersistTask;
import hs.mediasystem.persist.Persister;
import hs.mediasystem.util.AutoReentrantLock;
import hs.mediasystem.util.LifoBlockingDeque;
import hs.mediasystem.util.Task;
import hs.mediasystem.util.Task.TaskRunnable;
import hs.subtitle.DefaultThreadFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.collections.ObservableList;

/**
 * Context in which an Entity is used.  The Context affects whether or not and in which way
 * an Entity can be provided, enriched and/or persisted.
 */
// TODO remove dependency on javafx.application.Platform
public class EntityContext {
  private final ThreadPoolExecutor primaryExecutor = new ThreadPoolExecutor(5, 5, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>());
  private final ThreadPoolExecutor secondaryExecutor = new ThreadPoolExecutor(2, 2, 5, TimeUnit.SECONDS, new LifoBlockingDeque<Runnable>());
  private final Executor updateExecutor = new Executor() {
    @Override
    public void execute(Runnable command) {
      runLaterOnUpdateThread(command);
    }
  };

  private final PersistQueue persistQueue;

  private final AutoReentrantLock lock = new AutoReentrantLock();

  // TODO consider making entities weakly linked here
  private final Map<Class<? extends Entity>, Map<SourceKey, Entity>> entitiesByKeyByEntityClass = new HashMap<>();
  private final Map<Entity, Map<EntitySource, Object>> keysBySourceByEntity = new HashMap<>();

  private final Map<Class<? extends Entity>, Map<EntitySource, Persister<?, ?>>> persistersBySourceByEntityClass = new HashMap<>();
  private final Map<Class<? extends Entity>, Map<EntitySource, List<EnricherHolder>>> enrichersBySourceByEntityClass = new HashMap<>();
  private final Map<Class<? extends Entity>, Map<Class<? extends Entity>, Map<EntitySource, ListProvider<?, ?>>>> listProvidersBySourceByEntityClassByParentEntityClass = new HashMap<>();

  /**
   * Entities in this set are dirty and will be checked if persisting is needed at a later
   * point in time on the Update thread.  If they are removed from this Set before this
   * occurs, the persist action is cancelled.<p>
   *
   * This set may only be accessed from the Update thread.
   */
  private final Set<Entity> dirtyEntities = new HashSet<>();

  public EntityContext(PersistQueue persistQueue) {
    this.persistQueue = persistQueue;

    primaryExecutor.setThreadFactory(new DefaultThreadFactory("entityContext-primary-enricher", Thread.NORM_PRIORITY - 2, true));
    secondaryExecutor.setThreadFactory(new DefaultThreadFactory("entityContext-secondary-enricher", Thread.NORM_PRIORITY - 2, true));
  }

  public Executor getExecutor() {
    return primaryExecutor;
  }

  public Executor getSlowExecutor() {
    return secondaryExecutor;
  }

  public Executor getUpdateExecutor() {
    return updateExecutor;
  }

  public <E extends Entity> E add(Class<E> entityClass, Supplier<E> supplier, SourceKey... keys) {
    if(entityClass == null) {
      throw new IllegalArgumentException("Parameter 'entityClass' cannot be null");
    }
    if(keys.length == 0) {
      throw new IllegalArgumentException("Entity class must have atleast one key: " + entityClass);
    }

    try(AutoReentrantLock o = lock.lock()) {
      for(SourceKey key : keys) {
        E entity = fetch(entityClass, key);

        if(entity != null) {
          associate(entity, keys);
          return entity;
        }
      }

      E entity = createEntity(entityClass, supplier);

      entity.setContext(this);

      keysBySourceByEntity.put(entity, new HashMap<EntitySource, Object>());
      associate(entity, keys);

      return entity;
    }
  }

  public <E extends Entity> E add(Class<E> entityClass, SourceKey... keys) {
    return add(entityClass, null, keys);
  }

  private <E extends Entity> E createEntity(Class<E> entityClass, Supplier<E> supplier) {
    try {
      return supplier == null ? entityClass.newInstance() : supplier.get();
    }
    catch(InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public <E extends Entity> E fetch(Class<E> entityClass, SourceKey key) {
    try(AutoReentrantLock o = lock.lock()) {
      @SuppressWarnings("unchecked")
      Map<SourceKey, E> entitiesByKey = (Map<SourceKey, E>)entitiesByKeyByEntityClass.get(entityClass);

      return entitiesByKey == null ? null : entitiesByKey.get(key);
    }
  }

  public Object getKey(EntitySource source, Entity entity) {
    return keysBySourceByEntity.get(entity).get(source);
  }

  public void associate(Entity entity, SourceKey... keys) {
    try(AutoReentrantLock o = lock.lock()) {
      Map<EntitySource, Object> keysBySource = keysBySourceByEntity.get(entity);

      if(keysBySource == null) {
        throw new IllegalArgumentException("Entity is not part of this context, use Context#add instead: " + entity);
      }

      for(SourceKey key : keys) {
        keysBySource.put(key.getSource(), key.getKey());
        entitiesByKeyByEntityClass
          .computeIfAbsent(entity.getClass(), k -> new HashMap<>())
          .put(key, entity);
      }
    }
  }

  public <T extends Entity> void registerPersister(Class<T> entityClass, EntitySource source, Persister<T, ?> persister) {
    try(AutoReentrantLock o = lock.lock()) {
      persistersBySourceByEntityClass
        .computeIfAbsent(entityClass, k -> new HashMap<>())
        .put(source, persister);
    }
  }

  public <E extends Entity> void registerEnricher(Class<E> entityClass, EntitySource source, double priority, Enricher<E, Object> function) {
    try(AutoReentrantLock o = lock.lock()) {
      enrichersBySourceByEntityClass
        .computeIfAbsent(entityClass, k -> new HashMap<>())
        .compute(source, (k, v) -> {
          List<EnricherHolder> list = v == null ? new ArrayList<>() : v;

          list.add(new EnricherHolder(function, source, priority));
          list.sort(new Comparator<EnricherHolder>() {
            @Override
            public int compare(EnricherHolder o1, EnricherHolder o2) {
              return Double.compare(o1.getPriority(), o2.getPriority());
            }
          });

          return list;
        });
    }
  }

  public <P extends Entity, E extends Entity> void registerListProvider(Class<P> parentEntityClass, EntitySource source, Class<E> entityClass, ListProvider<P, ?> function) {
    try(AutoReentrantLock o = lock.lock()) {
      listProvidersBySourceByEntityClassByParentEntityClass
        .computeIfAbsent(parentEntityClass, k -> new HashMap<>())
        .computeIfAbsent(entityClass, k -> new HashMap<>())
        .put(source, function);
    }
  }

  /**
   * Marks the given Entity as clean so it does not trigger a persist action.  This can be called after an
   * Entity is updated from a persisted source to indicate that recent modifications donot require a persist.<p>
   *
   * Note that it is important that this occurs immediately after the end of the updates, running in the same step
   * on the Update thread.  Otherwise the updates and the mark clean may not be seen as atomic by other steps
   * running on the Update thread.<p>
   *
   * This must be called from the Update thread.
   *
   * @param entity Entity to mark clean
   */
  public void markClean(Entity entity) {
    ensureRunsOnUpdateThread();

    dirtyEntities.remove(entity);  // No need to lock dirtyEntities, always accessed from Update Thread
  }

  /**
   * Queues the given entity to be persisted in the background.  This must be called from the Update thread.
   *
   * @param entity Entity to persist
   */
  protected <T extends Entity> void queueAsDirty(T entity, Property<?> property) {
    ensureRunsOnUpdateThread();

    if(dirtyEntities.add(entity)) {  // No need to lock dirtyEntities, always accessed from Update Thread
      runLaterOnUpdateThread(new Runnable() {
        @Override
        public void run() {

          /*
           * If the Entity is still dirty, find any relevant Persisters and queue it up:
           */

          if(dirtyEntities.remove(entity)) {  // No need to lock dirtyEntities, always accessed from Update Thread
            try(AutoReentrantLock o = lock.lock()) {
              Class<?> cls = entity.getClass();

              while(cls != null) {
                Map<EntitySource, Persister<?, ?>> persistersBySource = persistersBySourceByEntityClass.get(cls);

                if(persistersBySource != null) {
                  Map<EntitySource, Object> keysBySource = keysBySourceByEntity.get(entity);

                  for(EntitySource source : persistersBySource.keySet()) {
                    @SuppressWarnings("unchecked")
                    Persister<T, Object> persister = (Persister<T, Object>)persistersBySource.get(source);

                    property.getValue();  // revalidates the property so subsequent accesses will re-queue the persist action in order to delay persisting until values have stabilised

                    persistQueue.queueAsDirty(entity, new PersistTask() {
                      @Override
                      public void persist() {
                        Object key = keysBySource == null ? null : keysBySource.get(source);

                        persister.persist(entity, key);
                      }
                    });
                  }
                }

                cls = cls.getSuperclass();
              }
            }
          }
        }
      });
    }
  }

  /**
   * Queues the given entity for enrichment in the background.  This must be called from the Update thread.
   *
   * @param entity entity to enrich
   */
  protected <T extends Entity> void queueForEnrichment(T entity) {
    ensureRunsOnUpdateThread();

    System.out.println("Enrichment needed for " + entity + ", loadState = " + entity.getLoadState());

    try(AutoReentrantLock o = lock.lock()) {
      Class<?> cls = entity.getClass();
      List<EnricherHolder> enricherHolders = new ArrayList<>();

      while(cls != null) {
        Map<EntitySource, List<EnricherHolder>> enrichersBySource = enrichersBySourceByEntityClass.get(cls);

        if(enrichersBySource != null) {
          for(List<EnricherHolder> list : enrichersBySource.values()) {
            enricherHolders.addAll(list);
          }
        }

        cls = cls.getSuperclass();
      }

      enricherHolders.sort((e1, e2) -> Double.compare(e1.getPriority(), e2.getPriority()));

      Map<EntitySource, Object> keysBySource = keysBySourceByEntity.get(entity);

      Task enrichTask = new Task();

      for(EnricherHolder holder : enricherHolders) {
        @SuppressWarnings("unchecked")
        Enricher<T, Object> enricher = (Enricher<T, Object>)holder.getEnricher();

        enrichTask.addStep(getExecutor(), new TaskRunnable() {
          @Override
          public void run(Task parent) {
            if(entity.getLoadState() != LoadState.FULL && keysBySource.containsKey(holder.getSource())) {
              enricher.enrich(EntityContext.this, parent, entity, keysBySource.get(holder.getSource()));
            }
          }
        });
      }

      if(enrichTask.hasSteps()) {
        enrichTask.addStep(getUpdateExecutor(), p -> entity.clearQueuedForEnrichment());
        getExecutor().execute(enrichTask);
      }
      else {
        System.out.println("[WARN] No Enrichers for: " + entity);
      }
    }
  }

  protected <P extends Entity, E extends Entity> void queueListProvide(P parentEntity, Class<E> itemClass, ObjectProperty<ObservableList<E>> property) {
    System.out.println("List Provide needed for " + parentEntity + "(" + parentEntity.getClass() + "): " + itemClass);

    try(AutoReentrantLock o = lock.lock()) {
      Task enrichTask = new Task();
      Class<? extends Entity> cls = parentEntity.getClass();

      for(;;) {
        Map<Class<? extends Entity>, Map<EntitySource, ListProvider<?, ?>>> listProvidersBySourceByEntityClass = listProvidersBySourceByEntityClassByParentEntityClass.get(cls);

        if(listProvidersBySourceByEntityClass != null) {
          Map<EntitySource, ListProvider<?, ?>> listProvidersBySource = listProvidersBySourceByEntityClass.get(itemClass);

          if(listProvidersBySource != null) {
            Iterator<Map.Entry<EntitySource, ListProvider<?, ?>>> iterator = listProvidersBySource
              .entrySet()
              .stream()
              .sorted((e1, e2) -> Double.compare(e1.getKey().getPriority(), e2.getKey().getPriority()))
              .iterator();

            while(iterator.hasNext()) {
              Map.Entry<EntitySource, ListProvider<?, ?>> e = iterator.next();
              @SuppressWarnings("unchecked")
              ListProvider<P, Object> function = (ListProvider<P, Object>)e.getValue();

              Map<EntitySource, Object> keysBySource = keysBySourceByEntity.get(parentEntity);

              if(keysBySource != null) {
                Object key = keysBySource.get(e.getKey());

                if(key != null) {
                  enrichTask.addStep(getExecutor(), p -> {
                    if(property.get() == null) {
                      function.provide(EntityContext.this, p, parentEntity, key);
                    }
                  });
                }
              }
            }
          }

          break;
        }

        if(!Entity.class.isAssignableFrom(cls.getSuperclass())) {
          break;
        }

        @SuppressWarnings("unchecked")
        Class<? extends Entity> entitySuperClass = (Class<? extends Entity>)cls.getSuperclass();
        cls = entitySuperClass;
      }

      if(!enrichTask.hasSteps()) {
        System.out.println("[WARN] No ListProviders for: " + itemClass + " in " + parentEntity + "; " + property);
      }
      else {
        getExecutor().execute(enrichTask);
      }
    }
  }

  public void ensureRunsOnUpdateThread() {
    if(!Platform.isFxApplicationThread()) {
      throw new IllegalStateException("Not on Update thread!");
    }
  }

  private void runLaterOnUpdateThread(Runnable runnable) {
    Platform.runLater(runnable);
  }

  public static class EnricherHolder {
    private final Enricher<?, ?> enricher;
    private final EntitySource source;
    private final double priority;

    public EnricherHolder(Enricher<?, ?> enricher, EntitySource source, double priority) {
      this.enricher = enricher;
      this.source = source;
      this.priority = priority;
    }

    public Enricher<?, ?> getEnricher() {
      return enricher;
    }

    public EntitySource getSource() {
      return source;
    }

    public double getPriority() {
      return priority;
    }
  }
}
