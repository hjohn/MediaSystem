package hs.mediasystem.framework;

import hs.mediasystem.enrich.DefaultEnrichable;
import hs.mediasystem.util.ImageHandle;

import java.util.Date;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Media extends DefaultEnrichable {
  private final StringProperty title = new SimpleStringProperty();
  public String getTitle() { return title.get(); }
  public StringProperty titleProperty() { return title; }

  private final StringProperty subtitle = new SimpleStringProperty("");
  public String getSubtitle() { return subtitle.get(); }
  public StringProperty subtitleProperty() { return subtitle; }

  private final ObjectProperty<Integer> releaseYear = new SimpleObjectProperty<>();
  public Integer getReleaseYear() { return releaseYear.get(); }
  public ObjectProperty<Integer> releaseYearProperty() { return releaseYear; }

  private final IntegerProperty runtime = new SimpleIntegerProperty() {
    @Override
    public int get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public int getRuntime() { return runtime.get(); }
  public IntegerProperty runtimeProperty() { return runtime; }

  private final ObjectProperty<String[]> genres = new SimpleObjectProperty<String[]>(new String[0]) {
    @Override
    public String[] get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public String[] getGenres() { return genres.get(); }
  public ObjectProperty<String[]> genresProperty() { return genres; }

  private final StringProperty description = new SimpleStringProperty() {
    @Override
    public String get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public String getDescription() { return description.get(); }
  public StringProperty descriptionProperty() { return description; }

  private final ObjectProperty<Date> releaseDate = new SimpleObjectProperty<Date>() {
    @Override
    public Date get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public Date getReleaseDate() { return releaseDate.get(); }
  public ObjectProperty<Date> releaseDateProperty() { return releaseDate; }

  private final DoubleProperty rating = new SimpleDoubleProperty() {
    @Override
    public double get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public Double getRating() { return rating.get(); }
  public DoubleProperty ratingProperty() { return rating; }

  private final ObjectProperty<ImageHandle> background = new SimpleObjectProperty<ImageHandle>() {
    @Override
    public ImageHandle get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public ImageHandle getBackground() { return background.get(); }
  public ObjectProperty<ImageHandle> backgroundProperty() { return background; }

  private final ObjectProperty<ImageHandle> banner = new SimpleObjectProperty<ImageHandle>() {
    @Override
    public ImageHandle get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public ImageHandle getBanner() { return banner.get(); }
  public ObjectProperty<ImageHandle> bannerProperty() { return banner; }

  private final ObjectProperty<ImageHandle> image = new SimpleObjectProperty<ImageHandle>() {
    @Override
    public ImageHandle get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public ImageHandle getImage() { return image.get(); }
  public ObjectProperty<ImageHandle> imageProperty() { return image; }

  public Media(String title, String subtitle, Integer releaseYear) {
    this.title.set(title == null ? "" : title);
    this.subtitle.set(subtitle == null ? "" : subtitle);
    this.releaseYear.set(releaseYear);
  }

  public Media(String title) {
    this(title, null, null);
  }
}
