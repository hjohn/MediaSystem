package hs.mediasystem.framework;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.Source;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichTaskProvider;
import hs.mediasystem.enrich.Enricher;
import hs.mediasystem.enrich.TaskKey;
import hs.mediasystem.fs.SourceImageHandle;
import hs.mediasystem.media.Media;
import hs.mediasystem.media.Movie;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class MovieEnricher implements Enricher<MediaItem, Movie> {
  private static final List<Class<?>> INPUT_PARAMETERS = new ArrayList<Class<?>>() {{
    add(MediaData.class);
    add(Media.class);
  }};

  private final ItemsDao itemsDao;
  private final TypeBasedItemEnricher typeBasedItemEnricher;

  @Inject
  public MovieEnricher(ItemsDao itemsDao, TypeBasedItemEnricher typeBasedItemEnricher) {
    this.itemsDao = itemsDao;
    this.typeBasedItemEnricher = typeBasedItemEnricher;
  }

  @Override
  public List<Class<?>> getInputTypes() {
    return INPUT_PARAMETERS;
  }

  @Override
  public EnrichTaskProvider<Movie> enrich(MediaItem key, Map<Class<?>, Object> inputParameters) {
    return new MovieEnrichTaskProvider(new TaskKey(key, Movie.class), (MediaData)inputParameters.get(MediaData.class), (Movie)inputParameters.get(Media.class));
  }

  private class MovieEnrichTaskProvider extends AbstractEnrichTaskProvider<Movie> {
    private final Movie currentMovie;

    public MovieEnrichTaskProvider(TaskKey taskKey, MediaData mediaData, Movie movie) {
      super(itemsDao, typeBasedItemEnricher, taskKey, mediaData);
      this.currentMovie = movie;
    }

    @Override
    public Movie itemToEnrichType(Item item) {
      Movie movie = new Movie(currentMovie.getGroupTitle(), currentMovie.getSequence(), currentMovie.getSubtitle(), item.getReleaseYear(), item.getImdbId());

      movie.backgroundProperty().set(createImageHandle(item.getBackground(), item, "background"));
      movie.imageProperty().set(createImageHandle(item.getPoster(), item, "poster"));
      movie.descriptionProperty().set(item.getPlot());
      movie.ratingProperty().set(item.getRating());
      movie.runtimeProperty().set(item.getRuntime());
      movie.genresProperty().set(item.getGenres());
      movie.releaseDateProperty().set(item.getReleaseDate());
      movie.languageProperty().set(item.getLanguage());
      movie.tagLineProperty().set(item.getTagline());
      movie.imdbNumberProperty().set(item.getImdbId());

      return movie;
    }
  }

  private static SourceImageHandle createImageHandle(Source<byte[]> source, Item item, String keyPostFix) {
    String key = "MovieEnricher:/" + item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getSubtitle() + "-" + item.getImdbId() + "-" + keyPostFix;

    return source == null ? null : new SourceImageHandle(source, key);
  }
}
