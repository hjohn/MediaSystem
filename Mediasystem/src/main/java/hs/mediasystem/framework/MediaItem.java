package hs.mediasystem.framework;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MediaItem {
  public final ObjectProperty<MediaData> mediaData = new SimpleObjectProperty<>();
  public final StringProperty uri = new SimpleStringProperty();

  public MediaItem(String uri) {
    assert uri != null;

    this.uri.set(uri);
  }

  public String getUri() {
    return uri.get();
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
