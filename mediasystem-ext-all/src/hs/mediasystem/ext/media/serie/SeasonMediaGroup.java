package hs.mediasystem.ext.media.serie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.DefaultMediaGroup;

import javax.inject.Named;

@Named @MediaRootType(SerieItem.class)
public class SeasonMediaGroup extends DefaultMediaGroup {

  public SeasonMediaGroup() {
    super("episodeNumber-group-season", "Season", new SeasonGrouper(), EpisodeComparator.INSTANCE, true, true);
  }

  @Override
  public Media<?> createMediaFromFirstItem(MediaItem item) {
    Integer season = (Integer)item.properties.get("season");

    return new Season(season == null || season == 0 ? "Specials" : "Season " + season);
  }

  @Override
  public String getShortTitle(MediaItem item) {
    Integer season = (Integer)item.properties.get("season");

    return season == null || season == 0 ? "Sp." : "" + season;
  }

  public static class Season extends Media<Season> {
    public Season(String title) {
      super(title);
    }
  }
}
