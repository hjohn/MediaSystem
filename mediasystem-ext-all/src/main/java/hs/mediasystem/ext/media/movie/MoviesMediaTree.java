package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.framework.EpisodeScanner;
import hs.mediasystem.framework.FileEntitySource;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.NameDecoder;
import hs.mediasystem.framework.NameDecoder.DecodeResult;
import hs.mediasystem.framework.NameDecoder.Hint;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.util.PathStringConverter;
import hs.mediasystem.util.Throwables;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.collections.ObservableList;

import javax.inject.Inject;

public class MoviesMediaTree implements MediaRoot {
  private static final Id ID = new Id("movieRoot");
  private static final NameDecoder NAME_DECODER = new NameDecoder(Hint.MOVIE);

  private final List<Path> roots;
  private final EntityContext entityContext;
  private final FileEntitySource fileEntitySource;

  private List<Movie> children;

  @Inject
  public MoviesMediaTree(FileEntitySource fileEntitySource, EntityContext entityContext, SettingsStore settingsStore) {
    this.fileEntitySource = fileEntitySource;
    this.entityContext = entityContext;

    ObservableList<Path> paths = settingsStore.getListProperty("MediaSystem:Ext:Movies", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

    this.roots = new ArrayList<>(paths);
  }

  @Override
  public List<? extends Media> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Path root : roots) {
        try {
          List<Path> scanResults = new EpisodeScanner(1).scan(root);

          for(final Path path : scanResults) {
            DecodeResult result = NAME_DECODER.decode(path.getFileName().toString());

            String title = result.getTitle();
            String sequence = result.getSequence();
            String subtitle = result.getSubtitle();
            Integer year = result.getReleaseYear();

            String imdb = result.getCode();
            String imdbNumber = imdb != null && !imdb.isEmpty() ? String.format("tt%07d", Integer.parseInt(imdb)) : null;

            Integer episode = sequence == null ? null : Integer.parseInt(sequence);

            Movie movie = entityContext.add(Movie.class, new Supplier<Movie>() {
              @Override
              public Movie get() {
                Movie movie = new Movie(new MediaItem(path.toString()));

                movie.initialTitle.set(title);
                movie.sequence.set(episode == null ? null : episode);
                movie.initialSubtitle.set(subtitle);
                movie.initialImdbNumber.set(imdbNumber);
                movie.localReleaseYear.set(year == null ? null : year.toString());

                return movie;
              }
            }, new SourceKey(fileEntitySource, path.toString()));

            // TODO think about pre-loading full items...

            children.add(movie);
          }
        }
        catch(RuntimeException e) {
          System.out.println("[WARN] " + getClass().getName() + "::getItems - Exception while getting items for \"" + root + "\": " + Throwables.formatAsOneLine(e));   // TODO add to some high level user error reporting facility
        }
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "Movies";
  }

  @Override
  public Id getId() {
    return ID;
  }

  @Override
  public MediaRoot getParent() {
    return null;
  }
}
