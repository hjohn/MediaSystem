package hs.mediasystem.framework;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MediaItem {
  public final ObjectProperty<MediaData> mediaData = new SimpleObjectProperty<>();

  private final String uri;

  public MediaItem(String uri) {
    assert uri != null;

    this.uri = uri;
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
    return "MediaItem[uri='" + uri +"']";
  }
}
