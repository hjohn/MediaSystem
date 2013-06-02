package hs.mediasystem.ext.media.movie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.DefaultMediaGroup;

import javax.inject.Named;

@Named @MediaRootType(MoviesMediaTree.class)
public class AlphaGroupByTitleMediaGroup extends DefaultMediaGroup {

  public AlphaGroupByTitleMediaGroup() {
    super("alpha-group-title", "Alphabetically, grouped by Title", new MovieGrouper(), MovieTitleGroupingComparator.INSTANCE, false, false);
  }

  @Override
  public Media<?> createMediaFromFirstItem(MediaItem item) {
    return new MovieGroup(item.getTitle());
  }
}
