package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import hs.mediasystem.entity.SimpleEntityProperty;
import hs.mediasystem.util.ImageHandle;

import java.time.LocalDate;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Media<T extends Media<T>> extends Entity<T> {
  public final StringProperty titleWithContext = stringProperty();
  public final StringProperty title = stringProperty();
  public final StringProperty subtitle = stringProperty("");
  public final StringProperty description = stringProperty();
  public final ObjectProperty<LocalDate> releaseDate = object("releaseDate");
  public final ObjectProperty<Integer> releaseYear = new SimpleObjectProperty<>();
  public final ObjectProperty<String[]> genres = object("genres");
  public final DoubleProperty rating = doubleProperty();
  public final IntegerProperty runtime = integerProperty();

  public final ObjectProperty<ImageHandle> image = object("image");
  public final ObjectProperty<ImageHandle> background = object("background");
  public final ObjectProperty<ImageHandle> banner = object("banner");

  public final SimpleEntityProperty<ObservableList<Casting>> castings = entity("castings");
  public final SimpleEntityProperty<ObservableList<Identifier>> identifiers = entity("identifiers");

  public Media(String initialTitle, String initialSubtitle, Integer initialReleaseYear) {
    title.set(initialTitle == null ? "" : initialTitle);
    titleWithContext.set(title.get());
    subtitle.set(initialSubtitle == null ? "" : initialSubtitle);
    releaseYear.set(initialReleaseYear);
  }

  public Media(String title) {
    this(title, null, null);
  }

  public Media() {
    this(null);
  }

  @Override
  public String toString() {
    return "Media('" + title.get() +"')";
  }
}
