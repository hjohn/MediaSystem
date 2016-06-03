package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import hs.mediasystem.framework.actions.Expose;
import hs.mediasystem.framework.descriptors.EntityDescriptors;
import hs.mediasystem.util.ImageHandle;
import hs.mediasystem.util.Levenshtein;
import hs.mediasystem.util.StringBinding;

import java.time.LocalDate;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;

public abstract class Media extends Entity {
  public final StringProperty prefix = stringProperty("prefix");
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
  public final StringProperty initialImdbNumber = stringProperty("initialImdbNumber");
  public final StringProperty initialTitle = stringProperty("initialTitle");
  public final StringProperty initialSubtitle = stringProperty("initialSubtitle", "");
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

    this.prefix.bind(new StringBinding(initialTitle) {
      @Override
      protected String computeValue() {
        return getPrefixAndTitle(initialTitle.get())[0];
      }
    });
    this.title.bind(new StringBinding(initialTitle) {
      @Override
      protected String computeValue() {
        return getPrefixAndTitle(initialTitle.get())[1];
      }
    });
    this.subtitle.bind(new StringBinding(enrichedTitle, prefix, title, initialSubtitle) {
      @Override
      protected String computeValue() {
        String initialSubtitleText = initialSubtitle.get();

        if(initialSubtitleText != null && !initialSubtitleText.isEmpty()) {
          return initialSubtitleText;
        }

        String enrichedTitleText = enrichedTitle.get();
        String prefixText = prefix.get();
        String titleText = title.get();

        if(enrichedTitleText != null && titleText != null) {
          double diff = Levenshtein.compare(enrichedTitleText.toLowerCase(), (prefixText != null && !prefixText.isEmpty() ? prefixText.toLowerCase() + " " : "") + titleText.toLowerCase());

          if(diff < 0.8) {
            return enrichedTitleText;
          }
        }

        return "";
      }
    });

    //this.title.bind(Bindings.when(enrichedTitle.isNull()).then(initialTitle).otherwise(enrichedTitle));
    this.titleWithContext.bind(title);
  }

  public Media(EntityDescriptors mediaProperties) {
    this(mediaProperties, null);
  }

  @Expose
  public void refresh(Event event) {
    mediaItem.get().mediaData.get().identifiers.get().clear();

    reidentify();
    event.consume();
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

  public String[] getPrefixAndTitle(String title) {
    if(title == null) {
      return new String[] {"", ""};
    }

    int comma = title.lastIndexOf(',');

    if(comma > 0) {
      String prefix = title.substring(comma + 1).trim();

      if(prefix.length() <= 3) {
        return new String[] {prefix, title.substring(0, comma)};
      }
    }

    return new String[] {"", title};
  }

  @Override
  public String toString() {
    return "Media['" + initialTitle.get() + "', mediaItem: " + mediaItem.get() + "]";
  }
}
