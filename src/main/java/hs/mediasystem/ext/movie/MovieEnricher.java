package hs.mediasystem.ext.movie;

import hs.mediasystem.db.Item;
import hs.mediasystem.db.ItemsDao;
import hs.mediasystem.db.MediaData;
import hs.mediasystem.db.Source;
import hs.mediasystem.db.TypeBasedItemEnricher;
import hs.mediasystem.enrich.EnrichTask;
import hs.mediasystem.enrich.Enricher;
import hs.mediasystem.enrich.Parameters;
import hs.mediasystem.framework.AbstractEnrichTaskProvider;
import hs.mediasystem.framework.TaskTitle;
import hs.mediasystem.fs.SourceImageHandle;
import hs.mediasystem.media.Media;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MovieEnricher implements Enricher<Movie> {
  private static final List<Class<?>> INPUT_PARAMETERS = new ArrayList<Class<?>>() {{
    add(TaskTitle.class);
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
  public List<EnrichTask<Movie>> enrich(Parameters parameters, boolean bypassCache) {
    List<EnrichTask<Movie>> enrichTasks = new ArrayList<>();

    MovieEnrichTaskProvider enrichTaskProvider = new MovieEnrichTaskProvider(parameters.unwrap(TaskTitle.class), parameters.get(MediaData.class), (Movie)parameters.get(Media.class));

    if(!bypassCache) {
      enrichTasks.add(enrichTaskProvider.getCachedTask());
    }
    enrichTasks.add(enrichTaskProvider.getTask(bypassCache));

    return enrichTasks;
  }

  private class MovieEnrichTaskProvider extends AbstractEnrichTaskProvider<Movie> {
    private final Movie currentMovie;

    public MovieEnrichTaskProvider(String title, MediaData mediaData, Movie movie) {
      super(title, itemsDao, typeBasedItemEnricher, mediaData);
      this.currentMovie = movie;
    }

    @Override
    public Movie itemToEnrichType(Item item) {
      Movie movie = new Movie(currentMovie.getGroupTitle(), currentMovie.getSequence(), currentMovie.getSubtitle(), currentMovie.getReleaseYear(), item.getImdbId());

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
    String key = "MovieEnricher:/" + item.getTitle() + "-" + item.getSeason() + "x" + item.getEpisode() + "-" + item.getImdbId() + "-" + keyPostFix;

    return source == null ? null : new SourceImageHandle(source, key);
  }
}
