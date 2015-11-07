package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.ext.media.serie.Episode;
import hs.mediasystem.framework.Identifier;
import hs.mediasystem.framework.MediaIdentifier;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class TmdbEpisodeMediaIdentifier extends MediaIdentifier<Episode> {
  private final TmdbEntitySource source;

  @Inject
  public TmdbEpisodeMediaIdentifier(TmdbEntitySource source) {
    super("TMDB", "Episode");

    this.source = source;
  }

  @Override
  public Identifier identify(Episode episode) {
    Object key = episode.getContext().getKey(source, episode.serie.get());

    if(key == null) {
      throw new IllegalStateException("serie key cannot be null for: " + source + ": " + episode);
    }
    if(episode.season.get() == null) {
      throw new IllegalStateException("season cannot be null for: " + source + ": " + episode);
    }
    if(episode.episodeRange.get() == null) {
      throw new IllegalStateException("episodeRange cannot be null for: " + source + ": " + episode);
    }

    String episodeKey = key + ";" + episode.season.get() + ";" + episode.episodeRange.get();

    return new Identifier().setAll(
      new ProviderId("Episode", "TMDB", episodeKey),
      MatchType.ID,
      1.0f
    );
  }
}
