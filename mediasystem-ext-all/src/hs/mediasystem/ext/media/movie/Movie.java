package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

public class Movie extends Media {
  public final ObjectProperty<Integer> sequence = new SimpleObjectProperty<>();
  public final StringProperty language = stringProperty("language");
  public final StringProperty tagLine = stringProperty("tagLine");
  public final StringProperty groupTitle = stringProperty("groupTitle");

  public Movie(MediaItem mediaItem) {
    super(mediaItem);
  }

  public Movie() {
    this(null);
  }
}
