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
public class AlphaGroupByTitleMediaGroup extends AbstractMediaGroup {

  public AlphaGroupByTitleMediaGroup() {
    super("alpha-group-title", "Alphabetically, grouped by Title", false);
  }

  @Override
  public List<MediaNode> getMediaNodes(MediaRoot mediaRoot, List<? extends MediaItem> mediaItems) {
    Collections.sort(mediaItems, MovieTitleGroupingComparator.INSTANCE);
    List<MediaNode> nodes = new ArrayList<>();

    for(MediaItem mediaItem : mediaItems) {
      MediaNode previousNode = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);

      /*
       * If the previous node has the same title, then the current item is grouped with
       * the previous item, otherwise just add it normally.
       */

      if(previousNode != null && mediaItem.getTitle().equals(previousNode.title.get())) {
        if(!(previousNode.getMedia() instanceof MovieGroup)) {
          MediaNode groupNode = new MediaNode("alpha-group-title[" + mediaItem.getTitle() + "]", new MovieGroup(mediaItem.getTitle()), null);

          nodes.set(nodes.size() - 1, groupNode);

          groupNode.addChild(previousNode);

          previousNode = groupNode;
        }

        previousNode.addChild(new MediaNode(mediaItem));
      }
      else {
        nodes.add(new MediaNode(mediaItem));
      }
    }

    return nodes;
  }
}
