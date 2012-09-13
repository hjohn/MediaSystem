package hs.mediasystem.framework;

import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.MediaData;
import hs.mediasystem.entity.Entity;
import hs.mediasystem.entity.SimpleEntityProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class MediaItem extends Entity<MediaItem> {
  public final SimpleEntityProperty<MediaData> mediaData = entity("mediaData");
  public final SimpleEntityProperty<Identifier> identifier = entity("identifier");
  public final ObjectProperty<Media<?>> media = object("media");

  private final String uri;
  private final String mediaType;

  public MediaItem(String uri, Media<?> media) {
    assert uri != null;

    this.uri = uri;
    this.media.set(media);
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

  /**
   * @param cls used for type specification only
   */
  @SuppressWarnings("unchecked")
  public <T extends Media<T>> T get(Class<T> cls) {
    return (T)getMedia();
  }

  public Media<?> getMedia() {
    return media.get();
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
