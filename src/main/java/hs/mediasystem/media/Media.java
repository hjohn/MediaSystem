package hs.mediasystem.media;

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

public class Media extends EnrichableDataObject {
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

  private final ObjectProperty<ImageHandle> image = new SimpleObjectProperty<ImageHandle>() {
    @Override
    public ImageHandle get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public ImageHandle getImage() { return image.get(); }
  public ObjectProperty<ImageHandle> imageProperty() { return image; }

  private final String key;

  public Media(Key key, String title, String subtitle, Integer releaseYear) {
    this.title.set(title == null ? "" : title);
    this.subtitle.set(subtitle == null ? "" : subtitle);
    this.releaseYear.set(releaseYear);

    this.key = key == null ? null : getClass().getSimpleName() + ":" + key;
  }

  public Media(String title, String subtitle, Integer releaseYear) {
    this(null, title, subtitle, releaseYear);
  }

  public Media(Key key, String title) {
    this(key, title, null, null);
  }

  public Media(String title) {
    this(null, title, null, null);
  }

  protected static Key createKey(Object... parts) {
    String key = "";

    for(Object part : parts) {
      if(part == null) {
        key += "/";
      }
      else if(part instanceof String) {
        key += "/" + ((String)part).trim().toLowerCase();
      }
      else {
        key += "/" + part;
      }
    }

    return new Key(key);
  }

  public String getKey() {
    return key;
  }

  private static class Key {
    private final String key;

    public Key(String key) {
      this.key = key;
    }

    @Override
    public String toString() {
      return key;
    }
  }
}
