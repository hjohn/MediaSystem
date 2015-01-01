package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Source;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityListProvider;
import hs.mediasystem.entity.ListProvider;
import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.ext.media.movie.Movie;
import hs.mediasystem.ext.media.serie.Serie;
import hs.mediasystem.framework.Casting;
import hs.mediasystem.framework.Casting.MediaType;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.Person;
import hs.mediasystem.framework.SourceImageHandle;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;

@EntityListProvider(parentEntityClass = Person.class, entityClass = Casting.class, sourceClass = TmdbEntitySource.class)
public class TmdbPersonCreditsListProvider implements ListProvider<Person, String> {
  private final TmdbEntitySource source;
  private final TheMovieDatabase tmdb;

  @Inject
  public TmdbPersonCreditsListProvider(TmdbEntitySource source, TheMovieDatabase tmdb) {
    this.source = source;
    this.tmdb = tmdb;
  }

  @Override
  public CompletableFuture<Void> provide(EntityContext context, Person person, String key) {
    return CompletableFuture
      .supplyAsync(() -> tmdb.query("3/person/" + key + "/combined_credits"), TheMovieDatabase.EXECUTOR)
      .thenAcceptAsync(node -> {
        ObservableList<Casting> castings = FXCollections.observableArrayList();
        Map<Media, Casting> castingsByMedia = new HashMap<>();

        for(Iterator<JsonNode> i = node.path("cast").iterator(); i.hasNext(); ) {
          JsonNode cast = i.next();
          Casting casting = createCasting(context, person, cast);

          Casting existingCasting = castingsByMedia.get(casting.media.get());

          if(existingCasting != null) {

            /*
             * Casting looks to be for a Media seen before -- combine it.
             */

            String characterNames = existingCasting.characterName.get();

            if(characterNames == null) {
              characterNames = "";
            }

            Set<String> set = new LinkedHashSet<>(Arrays.asList(characterNames.split("/")));

            set.add(casting.characterName.get());

            StringBuilder sb = new StringBuilder();

            for(String characterName : set) {
              if(sb.length() > 0) {
                sb.append(" / ");
              }

              if(characterName != null && !characterName.isEmpty()) {
                sb.append(characterName);
              }
            }

            if(sb.length() > 0) {
              existingCasting.characterName.set(sb.toString());
            }
          }
          else {
            castings.add(casting);
            castingsByMedia.put(casting.media.get(), casting);
          }
        }

        person.castings.set(castings);
      }, context.getUpdateExecutor());
  }

  private Casting createCasting(EntityContext context, Person person, JsonNode node) {
    MediaType mediaType = node.get("media_type").asText().equals("tv") ? MediaType.TV : MediaType.MOVIE;

    Class<? extends Media> mediaClass = mediaType == MediaType.TV ? Serie.class : Movie.class;
    String titleFieldName = mediaType == MediaType.TV ? "name" : "title";
    String title = node.get(titleFieldName).asText();

    Media media = context.add(mediaClass, new SourceKey(source, node.get("id").asText()));

    media.initialTitle.set(title);
    media.enrichedTitle.set(title);
    media.releaseDate.set(TheMovieDatabase.parseDateOrNull((mediaType == MediaType.TV ? node.path("first_air_date") : node.path("release_date")).getTextValue()));

    Source<byte[]> largestImageSource = tmdb.createSource(tmdb.createImageURL(node.path("poster_path").getTextValue(), "original"));  // This shouldn't be slow, so safe to run on Update thread

    if(largestImageSource != null) {
      media.image.set(new SourceImageHandle(largestImageSource, createImageKey(node, title, "poster")));
    }

    String character = node.path("character").getTextValue();
    String role = character.trim().matches("(?i).*\\b(as )?(himself|herself)\\b.*") ? "Self" : "Actor";

    Casting casting = new Casting();

    casting.media.set(media);
    casting.person.set(person);
    casting.characterName.set(character);
    casting.index.set(0);  // TODO no order, need to think of a nice casting order
    casting.role.set(role);
    casting.mediaType.set(mediaType);
    casting.episodeCount.set(mediaType  == MediaType.TV ? node.path("episode_count").getIntValue() : 0);

    return casting;
  }

  private static String createImageKey(JsonNode movieInfo, String title, String keyPostFix) {
    return "Media:/" + title + "-" + movieInfo.get("id").asText() + "-" + keyPostFix;
  }
}
