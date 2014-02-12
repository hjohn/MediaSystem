package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import hs.mediasystem.util.ImageHandle;

import java.time.LocalDate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Media extends Entity {
  public final StringProperty titleWithContext = stringProperty("titleWithContext");
  public final StringProperty title = stringProperty("title");
  public final StringProperty subtitle = stringProperty("subtitle", "");
  public final StringProperty description = stringProperty("description");
  public final ObjectProperty<LocalDate> releaseDate = object("releaseDate");
  public final ObjectProperty<String[]> genres = object("genres");
  public final DoubleProperty rating = doubleProperty("rating");
  public final IntegerProperty runtime = integerProperty("runtime");

  public StringProperty titleProperty() {
    return title;
  }

  public final ObjectProperty<ImageHandle> image = object("image");
  public final ObjectProperty<ImageHandle> background = object("background");
  public final ObjectProperty<ImageHandle> banner = object("banner");

  public final ObjectProperty<ObservableList<Casting>> castings = list("castings", Casting.class);
  public final ObjectProperty<ObservableList<Identifier>> identifiers = list("identifiers", Identifier.class);

  public Media setTitles(String initialTitle, String initialSubtitle) {
    title.set(initialTitle == null ? "" : initialTitle);
    titleWithContext.set(title.get());  // TODO solve with a binding please!  Setting just title should be sufficient.
    subtitle.set(initialSubtitle == null ? "" : initialSubtitle);

    return this;
  }

  public Media setTitle(String title) {
    return setTitles(title, null);
  }

  @Override
  public String toString() {
    return "Media('" + title.get() +"')";
  }
}
