package hs.mediasystem.ext.media.movie;

import javax.inject.Named;

import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.screens.MediaNodeCellProvider;

@Named
public class MovieMediaNodeCellProvider implements MediaNodeCellProvider {

  @Override
  public MediaNodeCell get() {
    return new MovieCell();
  }

  @Override
  public Class<?> getMediaType() {
    return Movie.class;
  }

  @Override
  public Type getType() {
    return Type.HORIZONTAL;
  }
}
