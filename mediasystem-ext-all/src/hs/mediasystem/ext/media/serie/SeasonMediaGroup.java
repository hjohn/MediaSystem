package hs.mediasystem.ext.media.serie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.ListMediaRoot;
import hs.mediasystem.framework.Media;
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

  public static class Season extends Media<Season> {
    public Season(String title) {
      super(title);
    }
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

    for(MediaItem mediaItem : mediaItems) {
      String seasonName = determineSeasonName(mediaItem);
      MediaNode seasonNode;

      if(seasonName.equals(previousSeasonName)) {
        seasonNode = nodes.get(nodes.size() - 1);
      }
      else {
        ListMediaRoot mediaRoot = new ListMediaRoot(parentMediaRoot, new Id("season"), seasonName);

        seasonNode = new MediaNode(mediaRoot, determineShortSeasonName(mediaItem), true, false, null);

        nodes.add(seasonNode);
      }

      ((ListMediaRoot)seasonNode.getMediaRoot()).add(mediaItem);

      previousSeasonName = seasonName;
    }

    return nodes;
  }
}
