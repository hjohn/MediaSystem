package hs.mediasystem.ext.enrich.tmdb;

import java.util.concurrent.CompletableFuture;

import hs.mediasystem.dao.Source;
import hs.mediasystem.entity.Enricher;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityEnricher;
import hs.mediasystem.framework.Person;
import hs.mediasystem.framework.SourceImageHandle;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;

@EntityEnricher(entityClass = Person.class, sourceClass = TmdbEntitySource.class, priority = 9.0)
public class TmdbPersonEnricher implements Enricher<Person, String> {
  private final TheMovieDatabase tmdb;

  @Inject
  public TmdbPersonEnricher(TheMovieDatabase tmdb) {
    this.tmdb = tmdb;
  }

  @Override
  public CompletableFuture<Void> enrich(EntityContext context, Person person, String tmdbId) {
    return CompletableFuture
      .supplyAsync(() -> {
        JsonNode node = tmdb.query("3/person/" + tmdbId);

        Source<byte[]> photoSource = tmdb.createSource(tmdb.createImageURL(node.path("profile_path").getTextValue(), "original"));

        return new UpdateTask(person, node, photoSource);
      }, TheMovieDatabase.EXECUTOR)
      .thenAcceptAsync(updateTask -> updateTask.run(), context.getUpdateExecutor());
  }

  private class UpdateTask {
    private final Person person;
    private final JsonNode node;
    private final Source<byte[]> photoSource;

    UpdateTask(Person person, JsonNode node, Source<byte[]> photoSource) {
      this.person = person;
      this.node = node;
      this.photoSource = photoSource;
    }

    public void run() {
      person.name.set(node.get("name").asText());
      person.biography.set(node.path("biography").getTextValue());
      person.birthPlace.set(node.path("place_of_birth").getTextValue());
      person.birthDate.set(TheMovieDatabase.parseDateOrNull(node.path("birthday").getTextValue()));

      if(photoSource != null) {
        person.photo.set(new SourceImageHandle(photoSource, "Person:/" + node.get("name").asText()));
      }
    }
  }
}
