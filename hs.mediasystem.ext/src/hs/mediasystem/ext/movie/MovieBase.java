package hs.mediasystem.ext.movie;

import hs.mediasystem.framework.DefaultEnrichable;
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
