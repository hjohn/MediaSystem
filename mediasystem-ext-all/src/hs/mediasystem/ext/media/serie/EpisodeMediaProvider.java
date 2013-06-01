package hs.mediasystem.ext.media.serie;

import javax.inject.Inject;
import javax.inject.Named;

import hs.mediasystem.dao.Item;
import hs.mediasystem.dao.ItemNotFoundException;
import hs.mediasystem.dao.ItemsDao;
import hs.mediasystem.dao.ProviderId;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaProvider;
import hs.mediasystem.framework.SourceImageHandle;

@Named
public class EpisodeMediaProvider extends MediaProvider<Episode> {
  private final SerieMediaProvider serieMediaProvider;
  private final ItemsDao itemsDao;

  @Inject
  public EpisodeMediaProvider(SerieMediaProvider serieMediaProvider, ItemsDao itemsDao) {
    this.serieMediaProvider = serieMediaProvider;
    this.itemsDao = itemsDao;
  }

  @Override
  protected Episode createMedia(Item item) {
    if(!item.getProviderId().getType().equals("Episode")) {
      return null;
    }

    try {
      Item serieItem = itemsDao.loadItem(new ProviderId("Serie", "TVDB", item.getProviderId().getId().split(",")[0]));
      Serie serie = serieMediaProvider.get(serieItem);

      return new Episode(serie, item.getTitle(), item.getSeason(), item.getEpisode(), item.getEpisode());
    }
    catch(ItemNotFoundException e) {
      System.out.println("[FINE] Exception while creating Episode entity: " + e);
      return null;
    }
  }

  @Override
  protected void configureMedia(Episode media, Item item) {
    media.background.set(item.getBackground() == null ? media.serie.get().background.get() : new SourceImageHandle(item.getBackground(), "Episode:/background/" + item.getId()));
  }

  @Override
  public Class<?> getType() {
    return Media.class;
  }
}
