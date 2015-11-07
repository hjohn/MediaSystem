package hs.mediasystem.ext.enrich.tmdb;

import hs.mediasystem.entity.EntitySource;

import javax.inject.Singleton;

@Singleton
public class TmdbEntitySource extends EntitySource {

  public TmdbEntitySource() {
    super("TMDB", 10.0, Integer.class);
  }
}
