package hs.mediasystem.ext.media.movie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.screens.DefaultMediaGroup;

import javax.inject.Named;

@Named @MediaRootType(MoviesMediaTree.class)
public class AlphaMediaGroup extends DefaultMediaGroup {

  public AlphaMediaGroup() {
    super("alpha", "Alphabetically", null, MovieTitleGroupingComparator.INSTANCE, false, false);
  }

  @Override
  public Media<?> createMediaFromFirstItem(MediaItem item) {
    return new MovieGroup(item.getTitle());
  }
}
