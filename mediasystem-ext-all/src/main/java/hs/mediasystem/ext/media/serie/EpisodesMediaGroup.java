package hs.mediasystem.ext.media.serie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.AbstractMediaGroup;
import hs.mediasystem.screens.MediaNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

@Named @MediaRootType(Serie.class)
public class EpisodesMediaGroup extends AbstractMediaGroup<Episode> {

  public EpisodesMediaGroup() {
    super("episodes", "Episodes", false);
  }

  @Override
  public List<MediaNode> getMediaNodes(MediaRoot parentMediaRoot, List<? extends Episode> episodes) {
    Collections.sort(episodes, EpisodeComparator.INSTANCE);
    List<MediaNode> nodes = new ArrayList<>();

    for(Episode episode : episodes) {
      nodes.add(new MediaNode(episode));
    }

    return nodes;
  }
}
