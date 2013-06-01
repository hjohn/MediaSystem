package hs.mediasystem.ext.media.serie;

import javax.inject.Named;

import hs.mediasystem.screens.MediaNodeCell;
import hs.mediasystem.screens.MediaNodeCellProvider;

@Named
public class SerieMediaNodeCellProvider implements MediaNodeCellProvider {

  @Override
  public MediaNodeCell get() {
    return new BannerCell();
  }

  @Override
  public Class<?> getMediaType() {
    return Serie.class;
  }

  @Override
  public Type getType() {
    return Type.HORIZONTAL;
  }
}
