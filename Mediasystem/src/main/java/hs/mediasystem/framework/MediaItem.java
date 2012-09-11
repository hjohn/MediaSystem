package hs.mediasystem.framework;

import hs.mediasystem.enrich.EnrichCache;
import hs.mediasystem.enrich.EnrichCache.CacheKey;
import hs.mediasystem.enrich.EnrichTrigger;
import hs.mediasystem.enrich.Enrichable;
import hs.mediasystem.enrich.EnrichmentListener;
import hs.mediasystem.enrich.EnrichmentState;
import hs.mediasystem.enrich.WeakEnrichmentListener;
import hs.mediasystem.persist.Persistable;
import hs.mediasystem.persist.Persister;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class MediaItem implements EnrichTrigger {
  private final ObservableMap<Class<?>, Object> data = FXCollections.observableMap(new HashMap<Class<?>, Object>() {
    @Override
    public Object get(Object key) {
      Object t = super.get(key);
      EnrichmentState enrichmentState = enrichmentStates.get(key);

      if(t == null && enrichmentState == null && getEnrichCache() != null) {
        getEnrichCache().enrich(cacheKey, (Class<?>)key);
      }

      return t;
    }
  });

  private final ObjectProperty<ObservableMap<Class<?>, Object>> dataMap = new SimpleObjectProperty<>(data);
  public ObjectProperty<ObservableMap<Class<?>, Object>> dataMapProperty() { return dataMap; }

  private final Map<Class<?>, EnrichmentState> enrichmentStates = new HashMap<>();

  private final MediaTree mediaTree;
  private final String uri;
  private final String mediaType;
  private final EnrichmentListener listener = new EnrichmentListener() {
    @Override
    public void update(EnrichmentState state, Class<?> enrichableClass, final Object enrichable) {
      enrichmentStates.put(enrichableClass, state);
      if(enrichable != null) {
        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            add(enrichable);
          }
        });
      }
    }
  };

  private CacheKey cacheKey;

  public MediaItem(MediaTree mediaTree, String uri, Object... data) {
    assert uri != null;

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
    if(o instanceof Enrichable) {
      ((Enrichable)o).setEnrichTrigger(this);
    }
    if(o instanceof Persistable) {
      @SuppressWarnings("unchecked")
      Persister<Object> persister = (Persister<Object>)PersisterProvider.getPersister(o.getClass());
      @SuppressWarnings("unchecked")
      Persistable<Object> persistable = (Persistable<Object>)o;
      persistable.setPersister(persister);
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

    return t;
  }

  public Media<?> getMedia() {
    return get(Media.class);
  }

  public EnrichmentState getEnrichmentState(Class<?> cls) {
    return enrichmentStates.get(cls);
  }

  public String getTitle() {
    return getMedia().title.get();
  }

  public String getMediaType() {
    return mediaType;
  }

  public String getId() {
    return uri;
  }

  public String getUri() {
    return uri;
  }

  protected MediaTree getMediaTree() {
    return mediaTree;
  }

  protected EnrichCache getEnrichCache() {
    return mediaTree.getEnrichCache();
  }

  public boolean isCachable() {
    return getEnrichCache() != null;
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
  public int hashCode() {
    return uri.hashCode();
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

    return uri.equals(other.uri);
  }

  @Override
  public String toString() {
    return "MediaItem[" + getMediaType() + ": '" + getMedia().title.get() + "' uri='" + uri +"']";
  }
}
