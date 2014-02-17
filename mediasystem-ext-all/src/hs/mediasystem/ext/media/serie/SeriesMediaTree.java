package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.dao.Setting.PersistLevel;
import hs.mediasystem.entity.EntityContext;
import hs.mediasystem.entity.SourceKey;
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

import javafx.collections.ObservableList;

import javax.inject.Inject;

public class SeriesMediaTree implements MediaRoot {
  private static final Id ID = new Id("serieRoot");

  private final FileEntitySource fileEntitySource;
  private final EntityContext entityContext;
  private final List<Path> roots;

  private List<Media> children;

  @Inject
  public SeriesMediaTree(FileEntitySource fileEntitySource, EntityContext entityContext, SettingsStore settingsStore) {
    this.fileEntitySource = fileEntitySource;
    this.entityContext = entityContext;

    ObservableList<Path> paths = settingsStore.getListProperty("MediaSystem:Ext:Series", PersistLevel.PERMANENT, "Paths", new PathStringConverter());

    this.roots = new ArrayList<>(paths);
  }

  @Override
  public List<? extends Media> getItems() {
    if(children == null) {
      children = new ArrayList<>();

      for(Path root : roots) {
        try {
          List<LocalInfo> scanResults = new SerieScanner().scan(root);

          for(LocalInfo localInfo : scanResults) {
            Serie child = entityContext.add(
              Serie.class,
              () -> {
                Serie serie = new Serie(SeriesMediaTree.this, new MediaItem(localInfo.getUri()), fileEntitySource);

                serie.localTitle.set(localInfo.getTitle());
                serie.subtitle.set(localInfo.getSubtitle());
                serie.localReleaseYear.set(localInfo.getReleaseYear() == null ? null : localInfo.getReleaseYear().toString());
                serie.imdbNumber.set(localInfo.getCode());

                return serie;
              },
              new SourceKey(fileEntitySource, localInfo.getUri())
            );

            children.add(child);
          }
        }
        catch(ScanException e) {
          System.err.println("[WARN] SeriesMediaTree: " + e.getMessage());  // TODO add to some high level user error reporting facility
          e.printStackTrace();
        }
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
