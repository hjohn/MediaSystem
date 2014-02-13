package hs.mediasystem.ext.media.movie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.AbstractMediaGroup;
import hs.mediasystem.screens.MediaNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

@Named @MediaRootType(MoviesMediaTree.class)
public class AlphaGroupByTitleMediaGroup extends AbstractMediaGroup<Movie> {

  public AlphaGroupByTitleMediaGroup() {
    super("alpha-group-title", "Alphabetically, grouped by Title", false);
  }

  @Override
  public List<MediaNode> getMediaNodes(MediaRoot parentMediaRoot, List<? extends Movie> movies) {
    Collections.sort(movies, MovieTitleGroupingComparator.INSTANCE);
    List<MediaNode> nodes = new ArrayList<>();
    MediaNode groupNode = null;

    for(Movie movie : movies) {
      MediaNode previousNode = nodes.isEmpty() ? null : nodes.get(nodes.size() - 1);

      /*
       * If the previous node has the same title, then the current item is grouped with
       * the previous item, otherwise just add it normally.
       */

      if(previousNode != null && movie.localTitle.get().equals(previousNode.media.get().localTitle.get())) {
        if(groupNode == null) {
          groupNode = new MediaNode("titleGroup[" + movie.localTitle.get() + "]", movie.localTitle.get(), null, false);

          nodes.set(nodes.size() - 1, groupNode);

          groupNode.add(new MediaNode(previousNode.getMedia()));

          previousNode = groupNode;
        }

        groupNode.add(new MediaNode(movie));
      }
      else {
        nodes.add(new MediaNode(movie));
        groupNode = null;
      }
    }

    return nodes;
  }
}
