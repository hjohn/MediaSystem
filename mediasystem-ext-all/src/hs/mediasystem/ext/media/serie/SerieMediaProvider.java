package hs.mediasystem.ext.media.serie;

import javax.inject.Named;

import hs.mediasystem.dao.Item;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaProvider;

@Named
public class SerieMediaProvider extends MediaProvider<Serie> {

  @Override
  protected Serie createMedia(Item item) {
    if(!item.getProviderId().getType().equals("Serie")) {
      return null;
    }

    return new Serie(item.getTitle());
  }

  @Override
  protected void configureMedia(Serie media, Item item) {
    // TODO Auto-generated method stub

  }

  @Override
  public Class<?> getType() {
    return Media.class;
  }
}
