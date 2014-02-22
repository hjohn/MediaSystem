package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Source;
import hs.mediasystem.entity.Enricher;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityEnricher;
import hs.mediasystem.ext.media.serie.Episode;
import hs.mediasystem.framework.SourceImageHandle;
import hs.mediasystem.util.Task;
import hs.mediasystem.util.Task.TaskRunnable;

import javax.inject.Inject;

import org.codehaus.jackson.JsonNode;

@EntityEnricher(entityClass = Episode.class, sourceClass = TmdbEntitySource.class, priority = 9.0)
public class TmdbEpisodeEnricher implements Enricher<Episode, String> {
  private final TheMovieDatabase tmdb;

  @Inject
  public TmdbEpisodeEnricher(TheMovieDatabase tmdb) {
    this.tmdb = tmdb;
  }

  @Override
  public void enrich(EntityContext context, Task parent, Episode episode, String tmdbId) {
    parent.addStep(TheMovieDatabase.EXECUTOR, new TaskRunnable() {
      @Override
      public void run(Task parent) {
        try {
          String[] id = tmdbId.split(";");
          JsonNode episodeInfo = tmdb.query("3/tv/" + id[0] + "/season/" + id[1] + "/episode/" + id[2]);

          Source<byte[]> backgroundSource = tmdb.createSource(tmdb.createImageURL(episodeInfo.path("backdrop_path").getTextValue(), "original"));
          Source<byte[]> posterSource = tmdb.createSource(tmdb.createImageURL(episodeInfo.path("still_path").getTextValue(), "original"));

          parent.addStep(context.getUpdateExecutor(), new UpdateTask(episode, episodeInfo, backgroundSource, posterSource));
        }
        catch(RuntimeException e) {
          e.printStackTrace();

          System.out.println("[WARN] TmdbEpisodeEnricher: unable to enrich Episode [id=" + tmdbId + "]: " + e.getMessage());
        }
      }
    });
  }

  private class UpdateTask implements TaskRunnable {
    private final Episode episode;
    private final JsonNode episodeInfo;
    private final Source<byte[]> backgroundSource;
    private final Source<byte[]> posterSource;

    UpdateTask(Episode episode, JsonNode episodeInfo, Source<byte[]> backgroundSource, Source<byte[]> posterSource) {
      this.episode = episode;
      this.episodeInfo = episodeInfo;
      this.backgroundSource = backgroundSource;
      this.posterSource = posterSource;
    }

    // TODO add support for specials / episode ranges
    @Override
    public void run(Task parent) {
      episode.enrichedTitle.set(episodeInfo.get("name").asText());
      episode.description.set(episodeInfo.path("overview").getTextValue());
      episode.rating.set(episodeInfo.path("vote_average").getDoubleValue());
      episode.releaseDate.set(TheMovieDatabase.parseDateOrNull(episodeInfo.path("air_date").getTextValue()));

      episode.background.set(backgroundSource == null ? episode.serie.get().background.get() : new SourceImageHandle(backgroundSource, createImageKey(episodeInfo, "background")));
      episode.image.set(posterSource == null ? null : new SourceImageHandle(posterSource, createImageKey(episodeInfo, "poster")));
    }
  }

  private static String createImageKey(JsonNode node, String keyPostFix) {
    return "Media:/" + node.get("name").asText() + "-" + node.get("id").asText() + "-" + keyPostFix;
  }
}
