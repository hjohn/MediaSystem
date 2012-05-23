package hs.mediasystem.framework;

import hs.mediasystem.media.EnrichableDataObject;
import hs.mediasystem.media.Media;

import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class MediaItem {
  public enum State {STANDARD, QUEUED, ENRICHED}

  private final ObservableMap<Class<?>, Object> data = FXCollections.observableHashMap();
  private final ObjectProperty<ObservableMap<Class<?>, Object>> dataMap = new SimpleObjectProperty<>(data);
  public ObjectProperty<ObservableMap<Class<?>, Object>> dataMapProperty() { return dataMap; }

  public void add(Object o) {
    if(o instanceof EnrichableDataObject) {
      ((EnrichableDataObject)o).setMediaItem(this);
    }

    Class<? extends Object> cls = o.getClass();
    do {
      data.put(cls, o);
      cls = cls.getSuperclass();
    } while(cls != null);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(Class<T> cls) {
    return (T)data.get(cls);
  }

  public Media getMedia() {
    return get(Media.class);
  }

  public String getTitle() {
    return getMedia() == null ? "(untitled)" : getMedia().getTitle();
  }

  private final MediaTree mediaTree;
  private final MediaItem parent;

  private State state = State.STANDARD;
  private int databaseId;

  private final String uri;

  private MediaItem(MediaTree mediaTree, MediaItem parent, String uri, Object... data) {
    for(Object o : data) {
      add(o);
    }

//    assert getMedia() != null;

    this.uri = uri;
    this.mediaTree = mediaTree;
    this.parent = parent;
  }

  public MediaItem(MediaTree mediaTree, String uri, boolean enrichable, Object... data) {
    this(mediaTree, null, uri, data);
    state = enrichable ? State.STANDARD : State.ENRICHED;
  }

  public MediaItem(MediaItem parent, String uri, Object... data) {
    this(parent.getMediaTree(), parent, uri, data);
  }

  public MediaItem(String mediaType, MediaTree mediaTree) {
    this(mediaTree, null, null);
    state = State.ENRICHED;
    this.mediaType = mediaType;
  }
  private String mediaType;

  public MediaItem getParent() {
    return parent;
  }

  public String getMediaType() {
    return mediaType == null ? getMedia().getClass().getSimpleName() : mediaType;
  }

  public String getUri() {
    return uri;
  }

  public State getState() {
    return state;
  }

  public synchronized void setEnriched() {
    this.state = State.ENRICHED;
  }

  public int getDatabaseId() {
    return databaseId;
  }

  public void setDatabaseId(int databaseId) {
    this.databaseId = databaseId;
  }

  @Override
  public String toString() {
    return "(" + getMediaType() + ": " + getMedia() + ")";
  }

  public boolean isLeaf() {
    return true;
  }

  public List<? extends MediaItem> children() {
    return Collections.emptyList();
  }

  public MediaTree getMediaTree() {
    return mediaTree;
  }

  public synchronized void queueForEnrichment() {
    if(state != State.ENRICHED) {
      mediaTree.queue(this);
      state = State.QUEUED;
    }
  }

  private final BooleanProperty viewed = new SimpleBooleanProperty() {
    @Override
    public boolean get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public boolean isViewed() { return viewed.get(); }
  public BooleanProperty viewedProperty() { return viewed; }
}
