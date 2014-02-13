package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import hs.mediasystem.util.ImageHandle;

import java.time.LocalDate;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public abstract class Media extends Entity {
  public final StringProperty title = stringProperty("title");
  public final StringProperty titleWithContext = stringProperty("titleWithContext");  // TODO this probably can be handle with MediaProperties
  public final StringProperty subtitle = stringProperty("subtitle", "");
  public final StringProperty description = stringProperty("description");
  public final ObjectProperty<LocalDate> releaseDate = object("releaseDate");
  public final ObjectProperty<String[]> genres = object("genres");
  public final DoubleProperty rating = doubleProperty("rating");
  public final IntegerProperty runtime = integerProperty("runtime");
  public final StringProperty imdbNumber = stringProperty("imdbNumber");
  public final StringProperty releaseYear = stringProperty("releaseYear");

  public final StringProperty externalTitle = stringProperty("externalTitle");
  public final StringProperty localTitle = stringProperty("localTitle");
  public final StringProperty localReleaseYear = stringProperty("localReleaseYear");

  public final ObjectProperty<ImageHandle> image = object("image");
  public final ObjectProperty<ImageHandle> background = object("background");
  public final ObjectProperty<ImageHandle> banner = object("banner");

  public final ObjectProperty<ObservableList<Casting>> castings = list("castings", Casting.class);
  public final ObjectProperty<ObservableList<Identifier>> identifiers = list("identifiers", Identifier.class);

  private final MediaItem mediaItem;

  public Media(MediaItem mediaItem) {
    this.mediaItem = mediaItem;

    this.releaseYear.bind(Bindings.when(releaseDate.isNull()).then(localReleaseYear).otherwise(Bindings.format("%tY", releaseDate)));
    this.title.bind(Bindings.when(externalTitle.isNull()).then(localTitle).otherwise(externalTitle));
    this.titleWithContext.bind(title);
  }

  public Media() {
    this(null);
  }

  /**
   * Returns the associated MediaItem if any.
   *
   * @return the associated MediaItem if any
   */
  public MediaItem getMediaItem() {
    return mediaItem;
  }

  @Override
  public String toString() {
    return "Media['" + localTitle.get() + "', mediaItem: " + mediaItem + "]";
  }
}
