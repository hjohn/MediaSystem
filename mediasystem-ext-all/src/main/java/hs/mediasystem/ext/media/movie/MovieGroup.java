package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.Media;

public class MovieGroup extends Media {

  public MovieGroup(String title) {
    super(null);

    this.initialTitle.set(title);
  }
}
