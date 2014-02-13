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
public class AlphaMediaGroup extends AbstractMediaGroup<Movie> {

  public AlphaMediaGroup() {
    super("alpha", "Alphabetically", false);
  }

  @Override
  public List<MediaNode> getMediaNodes(MediaRoot mediaRoot, List<? extends Movie> movies) {
    Collections.sort(movies, MovieTitleGroupingComparator.INSTANCE);
    List<MediaNode> nodes = new ArrayList<>();

    for(Movie movie : movies) {
      nodes.add(new MediaNode(movie));
    }

    return nodes;
  }
}
