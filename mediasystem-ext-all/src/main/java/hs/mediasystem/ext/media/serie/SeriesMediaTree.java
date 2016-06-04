package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.framework.FileEntitySource;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.NameDecoder;
import hs.mediasystem.framework.SettingsStore;
import hs.mediasystem.framework.NameDecoder.DecodeResult;
import hs.mediasystem.framework.NameDecoder.Hint;
import hs.mediasystem.util.PathStringConverter;
import hs.mediasystem.util.Throwables;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;

import javax.inject.Inject;

public class SeriesMediaTree implements MediaRoot {
  private static final Id ID = new Id("serieRoot");
  private static final NameDecoder NAME_DECODER = new NameDecoder(Hint.MOVIE, Hint.FOLDER_NAMES);

  private final FileEntitySource fileEntitySource;
  private final EntityContext entityContext;
  private final List<Path> roots;

  @Inject
  public SeriesMediaTree(FileEntitySource fileEntitySource, EntityContext entityContext, SettingsStore settingsStore) {
    this.fileEntitySource = fileEntitySource;
    this.entityContext = entityContext;

    ObservableList<Path> paths = settingsStore.getListProperty("MediaSystem:Ext:Series", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

    this.roots = new ArrayList<>(paths);
  }

  @Override
  public List<? extends Media> getItems() {
    List<Media> children = new ArrayList<>();

    for(Path root : roots) {
      try {
        List<Path> scanResults = new SerieScanner().scan(root);

        for(Path path : scanResults) {
          DecodeResult result = NAME_DECODER.decode(path.getFileName().toString());

          Serie child = entityContext.add(
            Serie.class,
            () -> {
              Serie serie = new Serie(SeriesMediaTree.this, new MediaItem(path.toString()), fileEntitySource);

              serie.initialTitle.set(result.getTitle());
              serie.initialSubtitle.set(result.getSubtitle());
              serie.localReleaseYear.set(result.getReleaseYear() == null ? null : result.getReleaseYear().toString());
              serie.initialImdbNumber.set(result.getCode());

              return serie;
            },
            new SourceKey(fileEntitySource, path.toString())
          );

          children.add(child);
        }
      }
      catch(RuntimeException e) {
        System.out.println("[WARN] " + getClass().getName() + "::getItems - Exception while getting items for \"" + root + "\": " + Throwables.formatAsOneLine(e));   // TODO add to some high level user error reporting facility
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return "Series";
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
