package hs.mediasystem.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hs.mediasystem.enrich.InstanceEnricher;
import hs.mediasystem.screens.Casting;
import hs.mediasystem.screens.Person;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EntityTest {
  private InstanceEnricher immediateEnricher;
  private InstanceEnricher slowEnricher;

  @Before
  public void before() {
    immediateEnricher = new InstanceEnricher() {
      @Override
      public void enrich(Object o) {
        if(o instanceof Person) {
          Person p = (Person)o;

          p.name.set("John Doe");
          p.birthPlace.set("Amsterdam");
        }
        else if(o instanceof Casting) {
          Casting c = (Casting)o;

          c.characterName.set("Alice");
          c.index.set(1);
          c.role.set("actor");
          c.person.set(new Person());
        }
      }

      @Override
      public void enrich(Object o, List<?> list, String listName) {
      }
    };

    slowEnricher = new InstanceEnricher() {
      @Override
      public void enrich(final Object o) {
        new Thread() {
          @Override
          public void run() {
            try {
              Thread.sleep(100);
              immediateEnricher.enrich(o);
            }
            catch(InterruptedException e) {
              throw new RuntimeException(e);
            }
          }
        }.start();
      }

      @Override
      public void enrich(Object o, List<?> list, String listName) {
      }
    };
  }

  @Test
  public void shouldEnrichWhenAccessed() {
    Person person = new Person();

    person.setEnricher(immediateEnricher);

    assertEquals("John Doe", person.name.get());
    assertEquals("Amsterdam", person.birthPlace.get());
  }

  @Test
  public void shouldEnrichListWhenAccessed() {

  }

  @Test
  public void shouldEnrichEntityWhenAccessed() {
    Casting casting = new Casting();

    casting.setEnricher(immediateEnricher);

    assertNotNull(casting.person.get());
  }

  @Test
  public void shouldAsyncEnrichEntityWhenAccessed() {
    Casting casting = new Casting();

    casting.setEnricher(slowEnricher);

    assertNull(casting.person.get());
    sleep(200);
    assertNotNull(casting.person.get());
  }

  protected void sleep(int millis) {
    try {
      Thread.sleep(millis);
    }
    catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
