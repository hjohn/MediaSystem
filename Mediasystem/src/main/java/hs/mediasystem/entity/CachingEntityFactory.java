package hs.mediasystem.entity;

import hs.mediasystem.db.DatabaseObject;
import hs.mediasystem.db.RecordMapper;
import hs.mediasystem.util.PropertyEq;
import hs.mediasystem.util.ServiceTracker;
import hs.mediasystem.util.WeakValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.osgi.framework.BundleContext;

public class CachingEntityFactory implements EntityFactory<DatabaseObject> {
  private static final Map<Class<?>, WeakValueMap<Object, Entity<?>>> INSTANCES = new HashMap<>();
  private static final Map<Class<?>, WeakHashMap<Entity<?>, DatabaseObject>> KEYS = new HashMap<>();

  private final ServiceTracker<EntityProvider<DatabaseObject, ?>> tracker;

  public CachingEntityFactory(BundleContext bundleContext) {
    tracker = new ServiceTracker<>(bundleContext, EntityProvider.class);
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

      List<EntityProvider<DatabaseObject, ?>> services = tracker.getServices(new PropertyEq("mediasystem.class", cls));

      for(EntityProvider<DatabaseObject, ?> provider : services) {
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