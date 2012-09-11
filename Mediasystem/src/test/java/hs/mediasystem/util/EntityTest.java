package hs.mediasystem.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hs.mediasystem.enrich.InstanceEnricher;
import hs.mediasystem.screens.Casting;
import hs.mediasystem.screens.Person;
import hs.mediasystem.test.JavaFXTestCase;

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
}
