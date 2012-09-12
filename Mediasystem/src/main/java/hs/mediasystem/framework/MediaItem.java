package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.persist.Persistable;
import hs.mediasystem.persist.Persister;

import java.util.HashMap;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class MediaItem extends Entity<MediaItem> {
  public final SimpleEntityProperty<MediaData> mediaData = entity("mediaData");
  public final SimpleEntityProperty<Identifier> identifier = entity("identifier");

  private final ObservableMap<Class<?>, Object> data = FXCollections.observableMap(new HashMap<Class<?>, Object>());

  private final ObjectProperty<ObservableMap<Class<?>, Object>> dataMap = new SimpleObjectProperty<>(data);
  public ObjectProperty<ObservableMap<Class<?>, Object>> dataMapProperty() { return dataMap; }

  private final MediaTree mediaTree;
  private final String uri;
  private final String mediaType;

  public MediaItem(MediaTree mediaTree, String uri, Object... data) {
    assert uri != null;

    this.uri = uri;
    this.mediaTree = mediaTree;

    for(Object o : data) {
      add(o);
    }

    this.mediaType = getMedia().getClass().getSimpleName();

    mediaData.addListener(new ChangeListener<MediaData>() {
      @Override
      public void changed(ObservableValue<? extends MediaData> observableValue, MediaData old, MediaData current) {
        if(current != null) {
          current.setPersister(PersisterProvider.getPersister(MediaData.class));
        }
      }
    });
  }

  private void add(Object o) {
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

  public void reloadMetaData() {
    //  getEnrichCache().reload(cacheKey); FIXME
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
