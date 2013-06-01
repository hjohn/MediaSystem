package hs.mediasystem.entity;

import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.db.RecordMapper;
import hs.mediasystem.util.WeakValueMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CachingEntityFactory implements EntityFactory<DatabaseObject> {
  private static final Map<Class<?>, WeakValueMap<Object, Entity<?>>> INSTANCES = new HashMap<>();
  private static final Map<Class<?>, WeakHashMap<Entity<?>, DatabaseObject>> KEYS = new HashMap<>();

  private final Set<EntityProvider<DatabaseObject, ?>> entityProviders;

  @Inject
  public CachingEntityFactory(Set<EntityProvider<DatabaseObject, ?>> entityProviders) {
    System.out.println(">>> CachingEntityFactory: Created, " + entityProviders);
    this.entityProviders = entityProviders;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Entity<?>> T create(Class<T> cls, DatabaseObject dbObject) {
    assert dbObject != null;
    assert dbObject.getDatabase() != null;

    synchronized(INSTANCES) {
      WeakValueMap<Object, Entity<?>> weakValueMap = INSTANCES.get(cls);
      Object key = extractKey(dbObject);

      if(weakValueMap != null) {
        Entity<?> entity = weakValueMap.get(key);

        if(entity != null) {
          return (T)entity;
        }
      }

     // List<EntityProvider<DatabaseObject, ?>> services = tracker.getServices(new PropertyEq("mediasystem.class", cls));

      for(EntityProvider<DatabaseObject, ?> provider : findEntityProviders(cls)) {
        T result = (T)provider.get(dbObject);

        if(result != null) {
          result.setEntityFactory(this);

          if(weakValueMap == null) {
            weakValueMap = new WeakValueMap<>();
            INSTANCES.put(cls, weakValueMap);
          }

          weakValueMap.put(key, result);

          WeakHashMap<Entity<?>, DatabaseObject> weakHashMap = KEYS.get(cls);

          if(weakHashMap == null) {
            weakHashMap = new WeakHashMap<>();
            KEYS.put(cls, weakHashMap);
          }

          weakHashMap.put(result, dbObject);

          return result;
        }
      }

      return null;
    }
  }

  public Set<EntityProvider<DatabaseObject, ?>> findEntityProviders(Class<?> cls) {
    Set<EntityProvider<DatabaseObject, ?>> matchingEntityProviders = new HashSet<>();

    for(EntityProvider<DatabaseObject, ?> entityProvider : entityProviders) {
      System.out.println("CEP: " + entityProvider.getType() + " vs " + cls + " == " + entityProvider.getType().equals(cls));

      if(entityProvider.getType().equals(cls)) {
        matchingEntityProviders.add(entityProvider);
      }
    }

    return matchingEntityProviders;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K extends DatabaseObject> K getAssociatedKey(Entity<?> entity) {
    synchronized(INSTANCES) {
      WeakHashMap<Entity<?>, DatabaseObject> weakHashMap = KEYS.get(entity.getClass());

      if(weakHashMap != null) {
        return (K)weakHashMap.get(entity);
      }

      return null;
    }
  }

  private static Object extractKey(DatabaseObject dbObject) {
    @SuppressWarnings("unchecked")
    RecordMapper<DatabaseObject> recordMapper = (RecordMapper<DatabaseObject>)dbObject.getDatabase().getRecordMapper(dbObject.getClass());

    Map<String, Object> extractIds = recordMapper.extractIds(dbObject);

    extractIds.put("-database-", dbObject.getDatabase());
    extractIds.put("-class-", dbObject.getClass());

    return extractIds;
  }
}