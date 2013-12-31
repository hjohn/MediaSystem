package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import hs.mediasystem.util.MapBindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class MediaItem extends Entity {
  public final StringProperty serieName = stringProperty("serieName");
  public final StringProperty title = stringProperty("title");
  public final StringProperty sequence = stringProperty("sequence");
  public final StringProperty subtitle = stringProperty("subtitle");

  public final ObjectProperty<Class<? extends Media>> dataType = object("dataType");  // TODO enforce read-only ness

  public final ObjectProperty<MediaData> mediaData = object("mediaData");
  public final ObjectProperty<Media> media = object("media");

  /**
   * Contains the stream title, which may be null if the stream is identified by some other means (sequence in a serie).
   */
  public final StringProperty streamTitle = stringProperty("streamTitle");

  public final ObservableMap<String, Object> properties = FXCollections.observableHashMap();

  private final String uri;

  public MediaItem(String uri, String serieName, String extractedTitle, String extractedSequence, String subtitle, Class<? extends Media> dataType) {
    assert uri != null;
    assert extractedSequence == null || !extractedSequence.equals("null");

    this.uri = uri;
    this.dataType.set(dataType);
    this.streamTitle.set(extractedTitle);

    this.serieName.set(serieName);
    this.sequence.set(extractedSequence);
    this.subtitle.set(subtitle);

    this.title.bind(new StringBinding() {
      StringBinding title = MapBindings.selectString(media, "title");

      {
        bind(streamTitle, title);
      }

      @Override
      protected String computeValue() {
        if(streamTitle.get() != null) {
          return streamTitle.get();
        }

        return title.get() == null ? sequence.get() : title.get();
      }
    });
  }

  public MediaItem(String uri, String title, Class<? extends Media> dataType) {
    this(uri, null, title, null, null, dataType);
  }

  /**
   * @param cls used for type specification only
   */
  @SuppressWarnings("unchecked")
  public <T extends Media> T get(Class<T> cls) {
    return (T)getMedia();
  }

  public Media getMedia() {
    return media.get();
  }

  public String getTitle() {
    return title.get();
  }

  public Class<?> getDataType() {
    return dataType.get();
  }

  public String getUri() {
    return uri;
  }

  public void reloadMetaData() {
    //  getEnrichCache().reload(cacheKey); TODO Fix option to reload metadata
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
    return "MediaItem['" + title.get() + "' uri='" + uri +"']";
  }
}
