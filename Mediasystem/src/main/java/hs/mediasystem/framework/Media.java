package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import hs.mediasystem.framework.descriptors.EntityDescriptors;
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
  public final IntegerProperty releaseYear = integerProperty("releaseYear");

  public final StringProperty enrichedTitle = stringProperty("enrichedTitle");
  public final StringProperty initialTitle = stringProperty("initialTitle");
  public final StringProperty localReleaseYear = stringProperty("localReleaseYear");

  public final ObjectProperty<ImageHandle> image = object("image");
  public final ObjectProperty<ImageHandle> background = object("background");
  public final ObjectProperty<ImageHandle> banner = object("banner");

  public final ObjectProperty<ObservableList<Casting>> castings = list("castings", Casting.class);
  public final ObjectProperty<ObservableList<Identifier>> identifiers = list("identifiers", Identifier.class);

  public final ObjectProperty<MediaItem> mediaItem = object("mediaItem");

  private final EntityDescriptors entityDescriptors;

  public Media(EntityDescriptors entityDescriptors, MediaItem mediaItem) {
    this.mediaItem.set(mediaItem);
    this.entityDescriptors = entityDescriptors;

    this.releaseYear.bind(Bindings.createIntegerBinding(() -> {
      return releaseDate.get() == null && localReleaseYear.get() == null ? 0 :
                                               releaseDate.get() == null ? Integer.parseInt(localReleaseYear.get()) : releaseDate.get().getYear();
    }, releaseDate, localReleaseYear));

    this.title.bind(Bindings.when(enrichedTitle.isNull()).then(initialTitle).otherwise(enrichedTitle));
    this.titleWithContext.bind(title);
  }

  public Media(EntityDescriptors mediaProperties) {
    this(mediaProperties, null);
  }

  public EntityDescriptors getEntityDescriptors() {
    return entityDescriptors;
  }

  /**
   * Returns the associated MediaItem if any.
   *
   * @return the associated MediaItem if any
   */
  public MediaItem getMediaItem() {
    return mediaItem.get();
  }

  @Override
  public String toString() {
    return "Media['" + initialTitle.get() + "', mediaItem: " + mediaItem.get() + "]";
  }
}
