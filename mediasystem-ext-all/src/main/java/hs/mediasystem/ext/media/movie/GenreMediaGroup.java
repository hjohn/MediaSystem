package hs.mediasystem.ext.media.movie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.AbstractMediaGroup;
import hs.mediasystem.screens.MediaNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.inject.Named;

@Named @MediaRootType(MoviesMediaTree.class)
public class GenreMediaGroup extends AbstractMediaGroup<Movie> {

  public GenreMediaGroup() {
    super("genre", "Alphabetically, grouped by Genre", false);
  }

  @Override
  public List<MediaNode> getMediaNodes(MediaRoot mediaRoot, List<? extends Movie> movies) {
    Collections.sort(movies, MovieTitleGroupingComparator.INSTANCE);
    LinkedHashMap<String, MediaNode> byGenre = new LinkedHashMap<>();

    for(String genreName : new String[] {"Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary", "Disaster", "Drama", "Fantasy", "History", "Horror", "Musical", "Mystery", "Romance", "Science Fiction", "Thriller", "War", "Western", "Uncategorized"}) {
      byGenre.put(genreName, new MediaNode("genre[" + genreName + "]", genreName, null, false));
    }

    for(Movie movie : movies) {
      boolean isUncategorized = true;
      String[] genreNames = movie.genres.get();

      if(genreNames != null) {
        for(String genreName : genreNames) {
          MediaNode genreNode = byGenre.get(genreName);

          if(genreNode != null) {
            genreNode.add(new MediaNode(movie));
            isUncategorized = false;
          }
        }
      }

      if(isUncategorized) {
        byGenre.get("Uncategorized").add(new MediaNode(movie));
      }
    }

    return new ArrayList<>(byGenre.values());
  }
}
