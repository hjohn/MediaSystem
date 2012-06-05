package hs.mediasystem.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import org.junit.Before;
import org.junit.Test;

public class MapBindingsTest {

  public static class File {
    private final ObjectProperty<Video> video = new SimpleObjectProperty<>();
    public ObjectProperty<Video> videoProperty() { return video; }

    public File(Video video) {
      this.video.set(video);
    }
  }

  public static class Video {
    private final StringProperty title = new SimpleStringProperty();
    public StringProperty titleProperty() { return title; }

    private final ObjectProperty<Episode> episode = new SimpleObjectProperty<>();
    public ObjectProperty<Episode> episodeProperty() { return episode; }

    private final ObservableMap<Class<?>, Object> map = FXCollections.observableHashMap();
    private final ObjectProperty<ObservableMap<Class<?>, Object>> dataMap = new SimpleObjectProperty<>(map);
    public ObjectProperty<ObservableMap<Class<?>, Object>> dataMapProperty() { return dataMap; }

    public Video(String title, Episode episode) {
      this.title.set(title);
      this.episode.set(episode);
    }
  }

  public static class Episode {
    private final IntegerProperty season = new SimpleIntegerProperty();
    public IntegerProperty seasonProperty() { return season; }

    public Episode(int season) {
      this.season.set(season);
    }
  }

  public static class YouTube {
    private final IntegerProperty rating = new SimpleIntegerProperty();
    public IntegerProperty ratingProperty() { return rating; }

    public YouTube(int rating) {
      this.rating.set(rating);
    }
  }

  private File file1;
  private Episode episode1;

  @Before
  public void before() {
    episode1 = new Episode(5);
    file1 = new File(new Video("Alice", episode1));
    file1.videoProperty().get().dataMapProperty().get().put(YouTube.class, new YouTube(9));
  }

  @Test
  public void shouldGetTitle() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "title");

    assertEquals("Alice", select.get());
  }

  @Test
  public void shouldGetSeason() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "episode", "season");

    assertEquals(5, select.get());
  }

  @Test
  public void shouldGetMapValue() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "dataMap", YouTube.class, "rating");

    assertEquals(9, select.get());
  }

  @Test
  public void shouldUpdateBindingWhenMapChanges() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "dataMap", YouTube.class, "rating");

    assertEquals(9, select.get());

    ObservableMap<Class<?>, Object> observableHashMap = FXCollections.observableHashMap();
    observableHashMap.put(YouTube.class, new YouTube(7));

    file1.videoProperty().get().dataMapProperty().set(observableHashMap);

    assertEquals(7, select.get());
  }

  @Test
  public void shouldUpdateBindingWhenVideoChanges() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "dataMap", YouTube.class, "rating");

    assertEquals(9, select.get());

    Video v = new Video("Something", null);
    v.dataMapProperty().get().put(YouTube.class, new YouTube(0));
    file1.videoProperty().set(v);

    assertEquals(0, select.get());
  }

  @Test
  public void shouldUpdateBindingWhenMapKeyChanges() {
    ObjectBinding<Object> select = MapBindings.select(file1.videoProperty(), "dataMap", YouTube.class, "rating");

    assertEquals(9, select.get());

    file1.videoProperty().get().dataMapProperty().get().put(YouTube.class, new YouTube(8));

    assertEquals(8, select.get());

    file1.videoProperty().get().dataMapProperty().get().remove(YouTube.class);

    assertNull(select.get());

    file1.videoProperty().get().dataMapProperty().get().put(YouTube.class, new YouTube(7));

    assertEquals(7, select.get());
  }

  @Test
  public void shouldReturnNullWhenAnyStepNull() {
    ObjectBinding<Object> seasonBinding = MapBindings.select(file1.videoProperty(), "episode", "season");
    ObjectBinding<Object> titleBinding = MapBindings.select(file1.videoProperty(), "title");
    ObjectBinding<Object> episodeBinding = MapBindings.select(file1.videoProperty(), "episode");

    assertEquals(5, seasonBinding.get());
    assertEquals("Alice", titleBinding.get());
    assertEquals(episode1, episodeBinding.get());

    file1.videoProperty().get().episodeProperty().set(null);

    assertNull(seasonBinding.get());
    assertEquals("Alice", titleBinding.get());
    assertNull(episodeBinding.get());

    file1.videoProperty().set(null);

    assertNull(seasonBinding.get());
    assertNull(titleBinding.get());
    assertNull(episodeBinding.get());
  }

  @Test
  public void shouldUpdateBindingWhenStepIsAltered() {
    ObjectBinding<Object> seasonBinding = MapBindings.select(file1.videoProperty(), "episode", "season");
    ObjectBinding<Object> titleBinding = MapBindings.select(file1.videoProperty(), "title");
    ObjectBinding<Object> episodeBinding = MapBindings.select(file1.videoProperty(), "episode");

    file1.videoProperty().set(null);

    assertNull(seasonBinding.get());
    assertNull(titleBinding.get());
    assertNull(episodeBinding.get());

    file1.videoProperty().set(new Video("Bob", null));

    assertNull(seasonBinding.get());
    assertEquals("Bob", titleBinding.get());
    assertNull(episodeBinding.get());

    file1.videoProperty().get().titleProperty().set("Cassie");

    assertNull(seasonBinding.get());
    assertEquals("Cassie", titleBinding.get());
    assertNull(episodeBinding.get());

    Episode episode15 = new Episode(15);
    file1.videoProperty().get().episodeProperty().set(episode15);

    assertEquals(15, seasonBinding.get());
    assertEquals("Cassie", titleBinding.get());
    assertEquals(episode15, episodeBinding.get());
  }
}
