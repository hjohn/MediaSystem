package hs.mediasystem.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hs.mediasystem.persist.PersistQueue;
import hs.mediasystem.persist.Persister;
import hs.mediasystem.test.JavaFXRunningRule;
import hs.mediasystem.util.MapBindings;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javafx.application.Platform;
import javafx.beans.binding.StringBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class EntityTest {
  @Rule
  public final JavaFXRunningRule jfxRunningRule = new JavaFXRunningRule();

  private static final EntitySource DB = new EntitySource("DB", 5.0, Integer.class);
  private static final EntitySource TMDB = new EntitySource("TMDB", 10.0, Integer.class);

  private static final Map<EntitySource, Map<Object, Object>> KEYS_BY_ENTITY_BY_SOURCE = new HashMap<>();

  private EntityContext context;

  @Before
  public void before() {
    context = new EntityContext(new PersistQueue(20));

    context.registerEnricher(Actor.class, TMDB, 9.0, new Enricher<Actor, Object>() {
      @Override
      public CompletableFuture<Void> enrich(EntityContext context, Actor entity, Object id) {
        return CompletableFuture
          .runAsync(() -> {
            if(id.toString().equals("5001")) {
              entity.name.set("John Doe");
              entity.biography.set("Biography");
              entity.birthDate.set(LocalDate.now().minus(28, ChronoUnit.YEARS));
              entity.birthPlace.set("Seatle");
              entity.father.set(context.add(Actor.class, new SourceKey(TMDB, "5002")));
            }
            else if(id.toString().equals("5002")) {
              entity.name.set("Jonathan Doe");
              entity.biography.set("Father's Biography");
              entity.birthDate.set(LocalDate.now().minus(55, ChronoUnit.YEARS));
              entity.birthPlace.set("New York");
            }
          }, context.getUpdateExecutor());
      }
    });

    context.registerEnricher(Actor.class, DB, 1.0, new Enricher<Actor, Object>() {
      @Override
      public CompletableFuture<Void> enrich(EntityContext context, Actor t, Object id) {
        return CompletableFuture
          .runAsync(() -> {
            if(id.toString().equals("301")) {
              t.biography.set("Biography from DB");
            }
            else if(id.toString().equals("302")) {
              t.biography.set("Biography from DB");
            }
            else if(id.toString().equals("304")) {
              t.getContext().associate(t, new SourceKey(TMDB, "5001"));
            }
          }, context.getUpdateExecutor());
      }
    });

    context.registerListProvider(Actor.class, DB, Casting.class, new ListProvider<Actor, Object>() {
      @Override
      public CompletableFuture<Void> provide(EntityContext context, Actor person, Object key) {
        if(key.toString().equals("303")) {
          return CompletableFuture
            .runAsync(() -> {
              person.castings.set(FXCollections.observableArrayList(
                new Casting(context.add(Media.class, new SourceKey(DB, "M1")), person, "Actor", "Han Solo", 1),
                new Casting(context.add(Media.class, new SourceKey(DB, "M2")), person, "Actor", "Indiana Jones", 0)
              ));
            }, context.getUpdateExecutor());
        }

        return CompletableFuture.completedFuture(null);
      }
    });

    context.registerPersister(Actor.class, DB, new Persister<Actor, Object>() {
      @Override
      public void persist(Actor entity, Object key) {
        KEYS_BY_ENTITY_BY_SOURCE
          .computeIfAbsent(DB, k -> new HashMap<>())
          .put(entity, key);
      }
    });
  }

  @Test
  public void shouldEnrichNotLoadedFieldsWhenAccessed() {
    Actor p = context.add(Actor.class, new SourceKey(TMDB, "5001"));

    runNowOnUpdateThread(() -> {
      p.name.set("John Doe");
      assertNull(p.biography.get());
    });

    waitUntilIdle();

    assertEquals("Biography", p.biography.get());
  }

  @Test
  public void shouldUseSecondaryProviderWhenPrimaryProviderFails() {
    Actor p = context.add(Actor.class, new SourceKey(DB, "301"), new SourceKey(TMDB, "5001"));

    runNowOnUpdateThread(() -> {
      p.name.set("John Doe");
      assertNull(p.biography.get());
    });

    waitUntilIdle();

    assertEquals("Biography", p.biography.get());
  }

  @Test
  public void shouldUseSecondaryProviderWhenPrimaryProviderOnlyProvidesNewKeys() {
    Actor p = context.add(Actor.class, new SourceKey(DB, "304"));

    runNowOnUpdateThread(() -> {
      p.name.set("John Doe");
      assertNull(p.biography.get());
    });

    waitUntilIdle();

    assertEquals("Biography", p.biography.get());
  }

  @Ignore("Currently support for multiple enrichers not needed in real code")
  @Test
  public void shouldNotUseSecondaryProviderWhenPrimaryProviderSucceeds() {
    Actor p = context.add(Actor.class, new SourceKey(DB, "302"), new SourceKey(TMDB, "5001"));

    runNowOnUpdateThread(() -> {
      p.name.set("John Doe");
      assertNull(p.biography.get());
    });

    waitUntilIdle();

    assertEquals("Biography from DB", p.biography.get());
  }

  @Test
  public void shouldEnrichTwice() throws InterruptedException {
    Actor p = context.add(Actor.class, new SourceKey(DB, "201"), new SourceKey(TMDB, "5001"));

    runNowOnUpdateThread(() -> {
      p.name.set("John Doe");
    });

    StringBinding fatherBiography = MapBindings.selectString(p.father, "biography");

    Platform.runLater(() ->
      fatherBiography.addListener(new ChangeListener<String>() {      // trigger double enrichment by registering a listener on the binding chain created above
        @Override
        public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        }
      })
    );

    Thread.sleep(200);

    assertEquals("Father's Biography", p.father.get().biography.get());
  }

  @Test
  public void shouldEnrichListWhenAccessed() {
    Actor p = context.add(Actor.class, new SourceKey(DB, "303"));

    runNowOnUpdateThread(() -> {
      p.name.set("John Doe");
      assertNull(p.castings.get());
    });

    waitUntilIdle();

    assertNotNull(p.castings.get());
    assertEquals(2, p.castings.get().size());
  }

  @Test
  public void shouldPersistChanges() throws InterruptedException {
    Actor p = context.add(Actor.class, new SourceKey(DB, "11"));

    runNowOnUpdateThread(() -> {
      p.name.set("John Doe");
      p.biography.set("New Biography");
    });

    Thread.sleep(200);

    assertNotNull(KEYS_BY_ENTITY_BY_SOURCE.get(DB));
    assertEquals("11", KEYS_BY_ENTITY_BY_SOURCE.get(DB).get(p));
  }

  private void waitUntilIdle() {
    try {
      Thread.sleep(1200);
    }
    catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private void runNowOnUpdateThread(Runnable runnable) {
    FutureTask<?> futureTask = new FutureTask<>(runnable, null);

    Platform.runLater(futureTask);

    try {
      futureTask.get();
    }
    catch(InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}
