package hs.mediasystem.ext.media.serie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.AbstractMediaGroup;
import hs.mediasystem.screens.MediaNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

@Named @MediaRootType(SerieItem.class)
public class SeasonMediaGroup extends AbstractMediaGroup {

  public SeasonMediaGroup() {
    super("episodeNumber-group-season", "Season", true);
  }

  private static String determineSeasonName(MediaItem item) {
    Integer season = (Integer)item.properties.get("season");

    return season == null || season == 0 ? "Specials" : "Season " + season;
  }

  private static String determineShortSeasonName(MediaItem item) {
    Integer season = (Integer)item.properties.get("season");

    return season == null || season == 0 ? "Sp." : "" + season;
  }

  @Override
  public List<MediaNode> getMediaNodes(MediaRoot parentMediaRoot, List<? extends MediaItem> mediaItems) {
    Collections.sort(mediaItems, EpisodeComparator.INSTANCE);
    List<MediaNode> nodes = new ArrayList<>();
    String previousSeasonName = null;
    MediaNode seasonNode = null;

    for(MediaItem mediaItem : mediaItems) {
      String seasonName = determineSeasonName(mediaItem);

      if(seasonNode == null || !seasonName.equals(previousSeasonName)) {
        seasonNode = new MediaNode("season[" + seasonName + "]", seasonName, determineShortSeasonName(mediaItem), false);

        nodes.add(seasonNode);
      }

      seasonNode.add(new MediaNode(mediaItem));

      previousSeasonName = seasonName;
    }

    return nodes;
  }
}
