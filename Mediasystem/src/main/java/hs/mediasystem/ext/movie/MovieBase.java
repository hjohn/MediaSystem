package hs.mediasystem.ext.movie;

import hs.mediasystem.framework.DefaultEnrichable;

public class MovieBase extends Movie {

  public MovieBase(String groupTitle, Integer sequence, String subtitle, Integer releaseYear, String imdbNumber) {
    super(groupTitle, sequence, subtitle, releaseYear, imdbNumber);
  }

  @Override
  protected Class<? extends DefaultEnrichable> getEnrichClass() {
    return Movie.class;
  }
}
