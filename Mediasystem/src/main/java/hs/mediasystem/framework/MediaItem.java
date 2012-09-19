package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import hs.mediasystem.entity.SimpleEntityProperty;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class MediaItem extends Entity<MediaItem> {
  public final SimpleEntityProperty<MediaData> mediaData = entity("mediaData");
  public final SimpleEntityProperty<Identifier> identifier = entity("identifier");
  public final ObjectProperty<Media<?>> media = object("media");

  public final StringProperty serieName = stringProperty();
  public final StringProperty title = stringProperty();
  public final StringProperty sequence = stringProperty();
  public final StringProperty subtitle = stringProperty();

  /**
   * Contains the stream title, which may be null if the stream is identified by some other means (sequence in a serie).
   */
  public final StringProperty streamTitle = stringProperty();

  private final String uri;
  private final String mediaType;

  public MediaItem(String uri, String serieName, String extractedTitle, String extractedSequence, String subtitle, Media<?> mediaParameter) {
    assert uri != null;

    this.uri = uri;

    this.streamTitle.set(extractedTitle);

    this.serieName.set(serieName);
    this.title.bind(new StringBinding() {
      {
        bind(streamTitle);
        bind(media);
      }

      @Override
      protected String computeValue() {
        if(streamTitle.get() != null) {
          return streamTitle.get();
        }

        return media.get() == null ? sequence.get() : media.get().title.get();
      }
    });
    this.sequence.set(extractedSequence);
    this.subtitle.set(subtitle);

    this.media.set(mediaParameter);
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

  public MediaItem(String uri, String title, Media<?> media) {
    this(uri, null, title, null, null, media);
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
