package hs.mediasystem.ext.media.serie;

import javax.inject.Named;

import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.screens.MediaNodeCellProvider;

@Named
public class EpisodeMediaNodeCellProvider implements MediaNodeCellProvider {

  @Override
  public MediaNodeCell get() {
    return new EpisodeCell();
  }

  @Override
  public Class<?> getMediaType() {
    return Episode.class;
  }

  @Override
  public Type getType() {
    return Type.HORIZONTAL;
  }
}
