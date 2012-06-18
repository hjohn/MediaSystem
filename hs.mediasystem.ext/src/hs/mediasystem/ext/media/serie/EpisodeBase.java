package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.DefaultEnrichable;
import hs.mediasystem.framework.Media;

public class EpisodeBase extends Episode {

  public EpisodeBase(SerieItem serie, String episodeName, Integer season, Integer episode, Integer endEpisode) {
    super(serie, episodeName, season, episode, endEpisode);
  }

  @Override
  protected Class<? extends DefaultEnrichable<Media>> getEnrichClass() {
    return Episode.class;
  }
}
