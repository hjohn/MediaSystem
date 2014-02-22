package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.framework.EpisodeScanner;
import hs.mediasystem.framework.FileEntitySource;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.ScanException;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.util.PathStringConverter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javafx.collections.ObservableList;

import javax.inject.Inject;

public class MoviesMediaTree implements MediaRoot {
  private static final Id ID = new Id("movieRoot");

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
          List<LocalInfo> scanResults = new EpisodeScanner(new MovieDecoder(), 1).scan(root);

          for(final LocalInfo localInfo : scanResults) {
            Movie movie = entityContext.add(Movie.class, new Supplier<Movie>() {
              @Override
              public Movie get() {
                Movie movie = new Movie(new MediaItem(localInfo.getUri()));

                movie.initialTitle.set(localInfo.getTitle());
                movie.sequence.set(localInfo.getEpisode() == null ? null : localInfo.getEpisode());
                movie.subtitle.set(localInfo.getSubtitle());
                movie.imdbNumber.set(localInfo.getCode());
                movie.localReleaseYear.set(localInfo.getReleaseYear() == null ? null : localInfo.getReleaseYear().toString());

                return movie;
              }
            }, new SourceKey(fileEntitySource, localInfo.getUri()));

            // TODO think about pre-loading full items...

            children.add(movie);
          }
        }
        catch(ScanException e) {
          System.err.println("[WARN] MoviesMediaTree: " + e.getMessage());  // TODO add to some high level user error reporting facility
          e.printStackTrace();
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
