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
    String episodeKey = episode.getContext().getKey(source, episode.serie.get()) + ";" + episode.season.get() + ";" + episode.episodeRange.get();

    return new Identifier().setAll(
      new ProviderId("Episode", "TMDB", episodeKey),
      MatchType.ID,
      1.0f
    );
  }
}
