package hs.mediasystem.ext.serie;

import hs.mediasystem.enrich.DefaultEnrichable;

public class EpisodeBase extends Episode {

  public EpisodeBase(SerieItem serie, String episodeName, Integer season, Integer episode, Integer endEpisode) {
    super(serie, episodeName, season, episode, endEpisode);
  }

  @Override
  protected Class<? extends DefaultEnrichable> getEnrichClass() {
    return Episode.class;
  }
}
