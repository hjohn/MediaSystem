package hs.mediasystem.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hs.mediasystem.dao.Identifier;
import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.entity.InstanceEnricher;
import hs.mediasystem.framework.Casting;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.Person;
import hs.mediasystem.test.JavaFXTestCase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import org.junit.Before;
import org.junit.Test;

public class EntityTest extends JavaFXTestCase {
  private InstanceEnricher<Casting, Void> castingEnricher;
  private InstanceEnricher<Person, Void> personEnricher;

  @Before
  public void before() {
    castingEnricher = new InstanceEnricher<Casting, Void>() {
      @Override
      public Void enrich(Casting c) {
        return null;
      }

      @Override
      public void update(Casting c, Void result) {
        c.characterName.set("Alice");
        c.index.set(1);
        c.role.set("actor");
        c.person.set(new Person());
      }
    };

    personEnricher = new InstanceEnricher<Person, Void>() {
      @Override
      public Void enrich(Person p) {
        return null;
      }

      @Override
      public void update(Person p, Void result) {
        p.name.set("John Doe");
        p.birthPlace.set("Amsterdam");
      }
    };
  }

  @Test
  public void shouldEnrichWhenAccessed() {
    Person person = new Person();

    person.setEnricher(personEnricher);

    assertNull(person.name.get());
    sleep(200);
    assertEquals("John Doe", person.name.get());
    assertEquals("Amsterdam", person.birthPlace.get());
  }

  @Test
  public void shouldEnrichListWhenAccessed() {

  }

  @Test
  public void shouldEnrichEntityWhenAccessed() {
    Casting casting = new Casting();

    casting.setEnricher(castingEnricher);

    assertNull(casting.person.get());
    sleep(200);
    assertNotNull(casting.person.get());
  }

  @Test
  public void shouldUpdateAsynchronously() {
    Movie movie = new Movie();

    final MediaItem mediaItem = new MediaItem(null, "L:\\SomeMovie.mkv", movie);

    mediaItem.identifier.setEnricher(new InstanceEnricher<MediaItem, Identifier>() {
      @Override
      public Identifier enrich(MediaItem parent) {
        return new Identifier(new ProviderId("MOVIE", "TMDB", "12256"), MatchType.HASH, 1.0f);
      }

      @Override
      public void update(MediaItem parent, Identifier result) {
        parent.identifier.set(result);
      }
    });

    movie.setEnricher(new InstanceEnricher<Movie, Identifier>() {
      @Override
      public Identifier enrich(final Movie m) {
        mediaItem.identifier.addListener(new ChangeListener<Identifier>() {
          @Override
          public void changed(ObservableValue<? extends Identifier> observableValue, Identifier old, Identifier current) {
            update(m, current);
          }
        });

        return mediaItem.identifier.get();
      }

      @Override
      public void update(Movie m, Identifier identifier) {
        if(identifier != null) {
          Item item = new Item();

          item.setRating(2.0f);
          item.setGenres(new String[] {"Action", "Adventure"});
          item.setPlot("A couple go underground and find true love");

          m.item.set(item);
        }
      }
    });

    assertNull(mediaItem.getMedia().description.get());
    sleep(200);
    assertEquals("A couple go underground and find true love", mediaItem.getMedia().description.get());
  }
}
