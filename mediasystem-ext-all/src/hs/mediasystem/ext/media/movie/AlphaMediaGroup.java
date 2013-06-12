package hs.mediasystem.ext.media.movie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.AbstractMediaGroup;
import hs.mediasystem.screens.MediaNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

@Named @MediaRootType(MoviesMediaTree.class)
public class AlphaMediaGroup extends AbstractMediaGroup {

  public AlphaMediaGroup() {
    super("alpha", "Alphabetically", false);
  }

  @Override
  public List<MediaNode> getMediaNodes(MediaRoot mediaRoot, List<? extends MediaItem> mediaItems) {
    Collections.sort(mediaItems, MovieTitleGroupingComparator.INSTANCE);
    List<MediaNode> nodes = new ArrayList<>();

    for(MediaItem mediaItem : mediaItems) {
      nodes.add(new MediaNode(mediaItem));
    }

    return nodes;
  }
}
