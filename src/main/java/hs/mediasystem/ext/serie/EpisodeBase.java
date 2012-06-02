package hs.mediasystem.ext.serie;

import hs.mediasystem.media.EnrichableDataObject;

public class EpisodeBase extends Episode {

  public EpisodeBase(SerieItem serie, String episodeName, Integer season, Integer episode, Integer endEpisode) {
    super(serie, episodeName, season, episode, endEpisode);
  }

  @Override
  protected Class<? extends EnrichableDataObject> getEnrichClass() {
    return Episode.class;
  }
}
