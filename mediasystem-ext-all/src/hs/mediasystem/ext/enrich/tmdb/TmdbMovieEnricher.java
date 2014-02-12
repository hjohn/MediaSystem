package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Source;
import hs.mediasystem.entity.Enricher;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityEnricher;
import hs.mediasystem.ext.media.movie.Movie;
import hs.mediasystem.framework.SourceImageHandle;
import hs.mediasystem.util.Task;
import hs.mediasystem.util.Task.TaskRunnable;

import java.util.List;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;

@EntityEnricher(entityClass = Movie.class, sourceClass = TmdbEntitySource.class, priority = 9.0)
public class TmdbMovieEnricher implements Enricher<Movie, String> {
  private final TheMovieDatabase tmdb;

  @Inject
  public TmdbMovieEnricher(TheMovieDatabase tmdb) {
    this.tmdb = tmdb;
  }

  @Override
  public void enrich(EntityContext context, Task parent, Movie movie, String tmdbId) {
    parent.addStep(TheMovieDatabase.EXECUTOR, new TaskRunnable() {
      @Override
      public void run(Task parent) {
        try {
          JsonNode movieInfo = tmdb.query("3/movie/" + tmdbId);

          Source<byte[]> backgroundSource = tmdb.createSource(tmdb.createImageURL(movieInfo.path("backdrop_path").getTextValue(), "original"));
          Source<byte[]> posterSource = tmdb.createSource(tmdb.createImageURL(movieInfo.path("poster_path").getTextValue(), "original"));

          parent.addStep(context.getUpdateExecutor(), new UpdateTask(movie, movieInfo, backgroundSource, posterSource));
        }
        catch(RuntimeException e) {
          e.printStackTrace();

          System.out.println("[WARN] TmdbMovieProvider: unable to enrich Movie [id=" + tmdbId + "]: " + e.getMessage());
        }
      }
    });
  }

  private class UpdateTask implements TaskRunnable {
    private final Movie movie;
    private final JsonNode movieInfo;
    private final Source<byte[]> backgroundSource;
    private final Source<byte[]> posterSource;

    UpdateTask(Movie movie, JsonNode movieInfo, Source<byte[]> backgroundSource, Source<byte[]> posterSource) {
      this.movie = movie;
      this.movieInfo = movieInfo;
      this.backgroundSource = backgroundSource;
      this.posterSource = posterSource;
    }

    @Override
    public void run(Task parent) {
      String imdbId = movieInfo.path("imdb_id").getTextValue();

      movie.title.set(movieInfo.get("title").asText());
      movie.description.set(movieInfo.path("overview").getTextValue());
      movie.tagLine.set(movieInfo.path("tagline").getTextValue());
      movie.rating.set(movieInfo.path("vote_average").getDoubleValue());
      movie.runtime.set(movieInfo.path("runtime").getIntValue());
      movie.imdbNumber.set(imdbId);        // TODO needs to be removed, handled differently now...

      List<String> spokenLanguages = movieInfo.path("spoken_languages").findValuesAsText("name");

      if(!spokenLanguages.isEmpty()) {
        StringBuilder languages = new StringBuilder();

        for(String spokenLanguage : spokenLanguages) {
          if(languages.length() > 0) {
            languages.append(", ");
          }

          languages.append(spokenLanguage);
        }

        movie.language.set(languages.toString());
      }

      movie.releaseDate.set(TheMovieDatabase.parseDateOrNull(movieInfo.path("release_date").getTextValue()));

      movie.background.set(backgroundSource == null ? null : new SourceImageHandle(backgroundSource, createImageKey(movieInfo, "background")));
      movie.image.set(posterSource == null ? null : new SourceImageHandle(posterSource, createImageKey(movieInfo, "poster")));

      List<String> genres = movieInfo.path("genres").findValuesAsText("name");

      movie.genres.set(genres.toArray(new String[genres.size()]));
    }
  }

  private static String createImageKey(JsonNode movieInfo, String keyPostFix) {
    return "Media:/" + movieInfo.get("title").asText() + "-" + movieInfo.get("id").asText() + "-" + keyPostFix;
  }
}
