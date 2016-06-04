package hs.mediasystem.ext.enrich.tmdb;

import java.util.concurrent.CompletableFuture;

import hs.mediasystem.dao.Source;
import hs.mediasystem.entity.Enricher;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.EntityEnricher;
import hs.mediasystem.ext.media.serie.Episode;
import hs.mediasystem.framework.SourceImageHandle;

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
  public CompletableFuture<Void> enrich(EntityContext context, Episode episode, String tmdbId) {
    return CompletableFuture
      .supplyAsync(() -> {
        String[] id = tmdbId.split(";");
        JsonNode episodeInfo = tmdb.query("3/tv/" + id[0] + "/season/" + id[1] + "/episode/" + id[2]);

        Source<byte[]> posterSource = tmdb.createSource(tmdb.createImageURL(episodeInfo.path("still_path").getTextValue(), "original"));

        return new UpdateTask(episode, episodeInfo, posterSource);
      }, TheMovieDatabase.EXECUTOR)
      .thenAcceptAsync(updateTask -> updateTask.run(), context.getUpdateExecutor());
  }

  private class UpdateTask {
    private final Episode episode;
    private final JsonNode episodeInfo;
    private final Source<byte[]> posterSource;

    UpdateTask(Episode episode, JsonNode episodeInfo, Source<byte[]> posterSource) {
      this.episode = episode;
      this.episodeInfo = episodeInfo;
      this.posterSource = posterSource;
    }

    // TODO add support for specials / episode ranges
    public void run() {
      episode.initialTitle.set(episodeInfo.get("name").asText());  // Series generally have poor titles, prefer title found from DB
      episode.description.set(episodeInfo.path("overview").getTextValue());
      episode.rating.set(episodeInfo.path("vote_average").getDoubleValue());
      episode.releaseDate.set(TheMovieDatabase.parseDateOrNull(episodeInfo.path("air_date").getTextValue()));

      episode.image.set(posterSource == null ? null : new SourceImageHandle(posterSource, createImageKey(episodeInfo, "poster")));
    }
  }

  private static String createImageKey(JsonNode node, String keyPostFix) {
    return "Media:/" + node.get("name").asText() + "-" + node.get("id").asText() + "-" + keyPostFix;
  }
}
