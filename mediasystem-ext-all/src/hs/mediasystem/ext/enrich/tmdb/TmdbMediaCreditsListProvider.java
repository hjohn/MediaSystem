package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Source;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityListProvider;
import hs.mediasystem.entity.ListProvider;
import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.ext.media.movie.Movie;
import hs.mediasystem.ext.media.serie.Episode;
import hs.mediasystem.ext.media.serie.Serie;
import hs.mediasystem.framework.Casting;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.Person;
import hs.mediasystem.framework.SourceImageHandle;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;

@EntityListProvider(parentEntityClass = Media.class, entityClass = Casting.class, sourceClass = TmdbEntitySource.class)
public class TmdbMediaCreditsListProvider implements ListProvider<Media, String> {
  private final TmdbEntitySource source;
  private final TheMovieDatabase tmdb;

  @Inject
  public TmdbMediaCreditsListProvider(TmdbEntitySource source, TheMovieDatabase tmdb) {
    this.source = source;
    this.tmdb = tmdb;
  }

  @Override
  public CompletableFuture<Void> provide(EntityContext context, Media media, String key) {
    return CompletableFuture
      .supplyAsync(() -> queryCredits(media, key), TheMovieDatabase.EXECUTOR)
      .thenAcceptAsync(node -> {
        ObservableList<Casting> castings = FXCollections.observableArrayList();

        for(Iterator<JsonNode> i = node.path("cast").iterator(); i.hasNext(); ) {
          JsonNode cast = i.next();

          castings.add(createCasting(context, media, cast, "Actor"));
        }

        for(Iterator<JsonNode> i = node.path("guest_stars").iterator(); i.hasNext(); ) {
          JsonNode cast = i.next();

          castings.add(createCasting(context, media, cast, "Guest Star"));
        }

        media.castings.set(castings);
      }, context.getUpdateExecutor());
  }

  private JsonNode queryCredits(Media media, String key) {
    if(media instanceof Movie) {
      return tmdb.query("3/movie/" + key + "/credits");
    }
    else if(media instanceof Serie) {
      return tmdb.query("3/tv/" + key + "/credits");
    }
    else if(media instanceof Episode) {
      String[] id = key.split(";");

      return tmdb.query("3/tv/" + id[0] + "/season/" + id[1] + "/episode/" + id[2] + "/credits");
    }

    throw new IllegalArgumentException("Unsupported Media type: " + media.getClass().getName());
  }

  private Casting createCasting(EntityContext context, Media media, JsonNode cast, String role) {
    Person person = context.add(Person.class, new SourceKey(source, cast.get("id").asText()));

    person.name.set(cast.get("name").asText());

    Source<byte[]> largestImageSource = tmdb.createSource(tmdb.createImageURL(cast.path("profile_path").getTextValue(), "original"));  // This shouldn't be slow, so safe to run on Update thread

    if(largestImageSource != null) {
      person.photo.set(new SourceImageHandle(largestImageSource, "Person:/" + cast.get("name").asText()));
    }

    Casting casting = new Casting();

    casting.media.set(media);
    casting.person.set(person);
    casting.characterName.set(cast.path("character").getTextValue());
    casting.index.set(cast.get("order").asInt());
    casting.role.set(role);

    return casting;
  }
}
