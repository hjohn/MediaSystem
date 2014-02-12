package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Source;
import hs.mediasystem.entity.Enricher;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityEnricher;
import hs.mediasystem.framework.Person;
import hs.mediasystem.framework.SourceImageHandle;
import hs.mediasystem.util.Task;
import hs.mediasystem.util.Task.TaskRunnable;

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
  public void enrich(EntityContext context, Task parent, Person person, String tmdbId) {
    parent.addStep(TheMovieDatabase.EXECUTOR, new TaskRunnable() {
      @Override
      public void run(Task parent) {
        try {
          JsonNode node = tmdb.query("3/person/" + tmdbId);

          Source<byte[]> photoSource = tmdb.createSource(tmdb.createImageURL(node.path("profile_path").getTextValue(), "original"));

          parent.addStep(context.getUpdateExecutor(), new UpdateTask(person, node, photoSource));
        }
        catch(RuntimeException e) {
          e.printStackTrace();

          System.out.println("[WARN] TmdbPersonEnricher: unable to enrich Person [id=" + tmdbId + "]: " + e.getMessage());
        }
      }
    });
  }

  private class UpdateTask implements TaskRunnable {
    private final Person person;
    private final JsonNode node;
    private final Source<byte[]> photoSource;

    UpdateTask(Person person, JsonNode node, Source<byte[]> photoSource) {
      this.person = person;
      this.node = node;
      this.photoSource = photoSource;
    }

    @Override
    public void run(Task parent) {
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
