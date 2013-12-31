package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Source;
import hs.mediasystem.entity.Enricher;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityEnricher;
import hs.mediasystem.entity.LoadState;
import hs.mediasystem.ext.media.serie.Serie;
import hs.mediasystem.framework.SourceImageHandle;
import hs.mediasystem.util.Task;
import hs.mediasystem.util.Task.TaskRunnable;

import java.util.List;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;

@EntityEnricher(entityClass = Serie.class, sourceClass = TmdbEntitySource.class, priority = 9.0)
public class TmdbSerieEnricher implements Enricher<Serie, String> {
  private final TheMovieDatabase tmdb;

  @Inject
  public TmdbSerieEnricher(TheMovieDatabase tmdb) {
    this.tmdb = tmdb;
  }

  @Override
  public void enrich(EntityContext context, Task parent, Serie serie, String tmdbId) {
    parent.addStep(TheMovieDatabase.EXECUTOR, new TaskRunnable() {
      @Override
      public void run(Task parent) {
        try {
          JsonNode serieInfo = tmdb.query("3/tv/" + tmdbId);

          Source<byte[]> backgroundSource = tmdb.createSource(tmdb.createImageURL(serieInfo.path("backdrop_path").getTextValue(), "original"));
          Source<byte[]> posterSource = tmdb.createSource(tmdb.createImageURL(serieInfo.path("poster_path").getTextValue(), "original"));

          parent.addStep(context.getUpdateExecutor(), new UpdateTask(serie, serieInfo, backgroundSource, posterSource));
        }
        catch(RuntimeException e) {
          e.printStackTrace();

          System.out.println("[WARN] TmdbMovieProvider: unable to enrich Movie [id=" + tmdbId + "]: " + e.getMessage());
        }
      }
    });
  }

  private class UpdateTask implements TaskRunnable {
    private final Serie serie;
    private final JsonNode serieInfo;
    private final Source<byte[]> backgroundSource;
    private final Source<byte[]> posterSource;

    public UpdateTask(Serie serie, JsonNode serieInfo, Source<byte[]> backgroundSource, Source<byte[]> posterSource) {
      this.serie = serie;
      this.serieInfo = serieInfo;
      this.backgroundSource = backgroundSource;
      this.posterSource = posterSource;
    }

    @Override
    public void run(Task parent) {
      serie.title.set(serieInfo.get("name").asText());
      serie.description.set(serieInfo.path("overview").getTextValue());
      serie.rating.set(serieInfo.path("vote_average").getDoubleValue());
      serie.runtime.set(serieInfo.path("episode_run_time").path(0).getIntValue());
      serie.releaseDate.set(TheMovieDatabase.parseDateOrNull(serieInfo.path("first_air_date").getTextValue()));

      serie.background.set(backgroundSource == null ? null : new SourceImageHandle(backgroundSource, createImageKey(serieInfo, "background")));
      serie.image.set(posterSource == null ? null : new SourceImageHandle(posterSource, createImageKey(serieInfo, "poster")));

      List<String> genres = serieInfo.path("genres").findValuesAsText("name");

      serie.genres.set(genres.toArray(new String[genres.size()]));
      serie.setLoadState(LoadState.FULL);
    }
  }

  private static String createImageKey(JsonNode movieInfo, String keyPostFix) {
    return "Media:/" + movieInfo.get("name").asText() + "-" + movieInfo.get("id").asText() + "-" + keyPostFix;
  }
}
