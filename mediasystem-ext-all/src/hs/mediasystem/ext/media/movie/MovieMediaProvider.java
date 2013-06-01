package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.Item;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaProvider;

import javax.inject.Named;

@Named
public class MovieMediaProvider extends MediaProvider<Movie> {
  @Override
  protected Movie createMedia(Item item) {
    if(!item.getProviderId().getType().equals("Movie")) {
      return null;
    }

    return new Movie(item.getTitle(), item.getEpisode(), "", null, item.getImdbId());
  }

  @Override
  protected void configureMedia(Movie media, Item item) {
    media.language.set(item.getLanguage());
    media.tagLine.set(item.getTagline());
    media.imdbNumber.set(item.getImdbId());
  }

  @Override
  public Class<?> getType() {
    return Media.class;
  }
}
