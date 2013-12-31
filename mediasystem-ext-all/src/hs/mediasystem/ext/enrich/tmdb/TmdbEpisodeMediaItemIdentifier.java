package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.dao.Identifier.MatchType;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.ext.media.serie.SerieItem;
import hs.mediasystem.framework.Identifier;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaItemIdentifier;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class TmdbEpisodeMediaItemIdentifier extends MediaItemIdentifier {
  private final TmdbEntitySource source;

  @Inject
  public TmdbEpisodeMediaItemIdentifier(TmdbEntitySource source) {
    super("TMDB", "Episode");

    this.source = source;
  }

  @Override
  public Identifier identify(MediaItem mediaItem) {
    SerieItem serieItem = (SerieItem)mediaItem.properties.get("serie");

    if(serieItem == null) {
      throw new IllegalArgumentException();
    }

    String episodeKey = mediaItem.getContext().getKey(source, serieItem.media.get()) + ";" + mediaItem.properties.get("season") + ";" + mediaItem.properties.get("episodeNumber");

    return new Identifier().setAll(
      new ProviderId("Episode", "TMDB", episodeKey),
      MatchType.ID,
      1.0f
    );
  }
}
