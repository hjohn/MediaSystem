package hs.mediasystem.fs;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.media.Movie;

public class MovieGrouper implements Grouper<MediaItem> {

  @Override
  public Object getGroup(MediaItem item) {
    Movie movie = item.get(Movie.class);

    if(movie.getSequence() != null) {
      return movie.getGroupTitle();
    }
    return movie.getGroupTitle() + "/" + movie.getReleaseYear();
  }
}
