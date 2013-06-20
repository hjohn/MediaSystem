package hs.mediasystem.ext.media.movie;

import hs.mediasystem.MediaRootType;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.AbstractMediaGroup;
import hs.mediasystem.screens.MediaNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

@Named @MediaRootType(MoviesMediaTree.class)
public class GenreMediaGroup extends AbstractMediaGroup {

  public GenreMediaGroup() {
    super("genre", "Alphabetically, grouped by Genre", false);
  }

  @Override
  public List<MediaNode> getMediaNodes(MediaRoot mediaRoot, List<? extends MediaItem> mediaItems) {
    Collections.sort(mediaItems, MovieTitleGroupingComparator.INSTANCE);
    LinkedHashMap<String, List<MediaItem>> byGenre = new LinkedHashMap<>();

    for(String genreName : new String[] {"Action", "Adventure", "Animation", "Comedy", "Crime", "Documentary", "Disaster", "Drama", "Fantasy", "History", "Horror", "Musical", "Mystery", "Romance", "Science Fiction", "Thriller", "War", "Western", "Uncategorized"}) {
      byGenre.put(genreName, new ArrayList<MediaItem>());
    }

    for(MediaItem mediaItem : mediaItems) {
      boolean isUncategorized = true;

      if(mediaItem.media.get() != null) {
        String[] genreNames = mediaItem.media.get().genres.get();

        for(String genreName : genreNames) {
          List<MediaItem> genreNodes = byGenre.get(genreName);

          if(genreNodes != null) {
            genreNodes.add(mediaItem);
            isUncategorized = false;
          }
        }
      }

      if(isUncategorized) {
        byGenre.get("Uncategorized").add(mediaItem);
      }
    }

    List<MediaNode> genres = new ArrayList<>();

    for(String genreName : byGenre.keySet()) {
      final List<MediaItem> genreChildren = byGenre.get(genreName);

      genres.add(new MediaNode(new Genre(mediaRoot, genreName, genreChildren), null, false, true, null));
    }

    return genres;
  }

  public static class Genre implements MediaRoot {
    private final Id id;
    private final MediaRoot mediaRoot;
    private final List<MediaItem> children;
    private final String title;

    public Genre(MediaRoot mediaRoot, String title, List<MediaItem> children) {
      this.id = new Id("genre");
      this.mediaRoot = mediaRoot;
      this.title = title;
      this.children = children;
    }

    @Override
    public Id getId() {
      return id;
    }

    @Override
    public String getRootName() {
      return title;
    }

    @Override
    public List<? extends MediaItem> getItems() {
      return children;
    }

    @Override
    public MediaRoot getParent() {
      return mediaRoot;
    }

    private static final Map<String, Object> MEDIA_PROPERTIES = new HashMap<>();

    @Override
    public Map<String, Object> getMediaProperties() {
      return Collections.unmodifiableMap(MEDIA_PROPERTIES);
    }
  }
}
