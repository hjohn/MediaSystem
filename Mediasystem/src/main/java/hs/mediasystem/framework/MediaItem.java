package hs.mediasystem.framework;

import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.enrich.EnrichCache.CacheKey;
import hs.mediasystem.enrich.EnrichTrigger;
import hs.mediasystem.enrich.Enrichable;
import hs.mediasystem.enrich.EnrichmentListener;
import hs.mediasystem.enrich.EnrichmentState;
import hs.mediasystem.enrich.WeakEnrichmentListener;
import hs.mediasystem.persist.PersistTrigger;
import hs.mediasystem.persist.Persistable;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class MediaItem implements EnrichTrigger, PersistTrigger {
  private final ObservableMap<Class<?>, Object> data = FXCollections.observableHashMap();
  private final ObjectProperty<ObservableMap<Class<?>, Object>> dataMap = new SimpleObjectProperty<>(data);
  public ObjectProperty<ObservableMap<Class<?>, Object>> dataMapProperty() { return dataMap; }

  private final Map<Class<?>, EnrichmentState> enrichmentStates = new HashMap<>();

  private final String id;
  private final MediaTree mediaTree;
  private final String uri;
  private final String mediaType;
  private final EnrichmentListener listener = new EnrichmentListener() {
    @Override
    public void update(EnrichmentState state, Class<?> enrichableClass, Object enrichable) {
      enrichmentStates.put(enrichableClass, state);
      if(enrichable != null) {
        add(enrichable);
      }
    }
  };

  private CacheKey cacheKey;
  private int databaseId;

  public MediaItem(MediaTree mediaTree, String uri, Object... data) {
    this.id = uri == null ? "hash:/" + super.hashCode() : "uri:/" + uri;
    this.uri = uri;
    this.mediaTree = mediaTree;

    for(Object o : data) {
      add(o);
    }

    this.mediaType = getMedia().getClass().getSimpleName();

    if(getEnrichCache() != null) {
      this.cacheKey = getEnrichCache().obtainKey(uri);

      getEnrichCache().insertImmutable(cacheKey, new TaskTitle(getTitle()));
      getEnrichCache().insertImmutable(cacheKey, new MediaItemUri(uri));

      for(Object o : data) {
        getEnrichCache().insertImmutableDataIfNotExists(cacheKey, o);
      }

      getEnrichCache().addListener(cacheKey, new WeakEnrichmentListener(listener));
    }
  }

  private void add(Object o) {
    assert this.id != null;

    if(o instanceof Enrichable) {
      ((Enrichable)o).setEnrichTrigger(this);
    }
    if(o instanceof Persistable) {
      ((Persistable)o).setPersistTrigger(this);
    }

    Class<? extends Object> cls = o.getClass();
    do {
      data.put(cls, o);
      cls = cls.getSuperclass();
    } while(cls != null);
  }

  public <T> T get(Class<T> cls) {
    @SuppressWarnings("unchecked")
    T t = (T)data.get(cls);

    EnrichmentState enrichmentState = enrichmentStates.get(cls);

    if(t == null && enrichmentState == null) {
      getEnrichCache().enrich(cacheKey, cls);
    }

    return t;
  }

  public Media getMedia() {
    return get(Media.class);
  }

  public EnrichmentState getEnrichmentState(Class<?> cls) {
    return enrichmentStates.get(cls);
  }

  public String getTitle() {
    return getMedia().getTitle();
  }

  public String getMediaType() {
    return mediaType;
  }

  public String getUri() {
    return uri;
  }

  public int getDatabaseId() {
    return databaseId;
  }

  public void setDatabaseId(int databaseId) {
    this.databaseId = databaseId;
  }

  public MediaTree getMediaTree() {
    return mediaTree;
  }

  public EnrichCache getEnrichCache() {
    return mediaTree.getEnrichCache();
  }

  public void reloadMetaData() {
    getEnrichCache().reload(cacheKey);
  }

  @Override
  public void queueForEnrichment(Class<? extends Enrichable> cls) {
    if(getEnrichCache() != null) {
      EnrichmentState enrichmentState = enrichmentStates.get(cls);

      if(enrichmentState == null || enrichmentState == EnrichmentState.IMMUTABLE) {
        getEnrichCache().enrich(cacheKey, cls);
      }
    }
  }

  @Override
  public void queueAsDirty(Persistable persistable) {
    if(getMediaTree().getPersister() != null) {
      getMediaTree().getPersister().queueAsDirty(persistable);
    }
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

    MediaItem other = (MediaItem)obj;

    return id.equals(other.id);
  }

  @Override
  public String toString() {
    return "MediaItem[" + getMediaType() + ": '" + getMedia().getTitle() + "' uri='" + uri +"']";
  }
}
