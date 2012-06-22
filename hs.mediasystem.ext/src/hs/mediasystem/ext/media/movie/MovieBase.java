package hs.mediasystem.ext.media.movie;

import hs.mediasystem.enrich.DefaultEnrichable;
import hs.mediasystem.framework.Media;

public class MovieBase extends Movie {

  public MovieBase(String groupTitle, Integer sequence, String subtitle, Integer releaseYear, String imdbNumber) {
    super(groupTitle, sequence, subtitle, releaseYear, imdbNumber);
  }

  @Override
  protected Class<? extends DefaultEnrichable<Media>> getEnrichClass() {
    return Movie.class;
  }
}
