package hs.mediasystem.ext.media.movie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.AbstractMediaGroup;
import hs.mediasystem.screens.MediaNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Named;

@Named @MediaRootType(MoviesMediaTree.class)
public class GenreMediaGroup extends AbstractMediaGroup {

  public GenreMediaGroup() {
    super("genre", "Alphabetically, grouped by Genre", false);
  }

  @Override
  public List<MediaNode> getMediaNodes(MediaRoot mediaRoot, List<? extends MediaItem> mediaItems) {
    Collections.sort(mediaItems, MovieTitleGroupingComparator.INSTANCE);
    LinkedHashMap<String, MediaNode> byGenre = new LinkedHashMap<>();

    for(String genreName : new String[] {"Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary", "Disaster", "Drama", "Fantasy", "History", "Horror", "Musical", "Mystery", "Romance", "Science Fiction", "Thriller", "War", "Western", "Uncategorized"}) {
      byGenre.put(genreName, new MediaNode("genre[" + genreName + "]", genreName, null, false));
    }

    for(MediaItem mediaItem : mediaItems) {
      boolean isUncategorized = true;

      if(mediaItem.media.get() != null) {
        String[] genreNames = mediaItem.media.get().genres.get();

        for(String genreName : genreNames) {
          MediaNode genreNode = byGenre.get(genreName);

          if(genreNode != null) {
            genreNode.add(new MediaNode(mediaItem));
            isUncategorized = false;
          }
        }
      }

      if(isUncategorized) {
        byGenre.get("Uncategorized").add(new MediaNode(mediaItem));
      }
    }

    return new ArrayList<>(byGenre.values());
  }
}
