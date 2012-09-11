package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.MediaItem;

public class MovieGrouper implements Grouper<MediaItem> {

  @Override
  public Object getGroup(MediaItem item) {
    Movie movie = item.get(Movie.class);

    if(movie.sequence.get() != null) {
      return movie.groupTitle.get();
    }
    return movie.groupTitle.get() + "/" + movie.releaseYear.get();
  }
}
