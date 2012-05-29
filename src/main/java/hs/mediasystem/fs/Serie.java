package hs.mediasystem.fs;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.media.Episode;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Serie extends MediaItem implements MediaRoot {
  private List<MediaItem> children;

  public Serie(MediaTree mediaTree, String uri, hs.mediasystem.media.Serie serie) {
    super(mediaTree, uri, serie);
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new EpisodeScanner(new EpisodeDecoder(getTitle())).scan(Paths.get(getUri()));

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        Episode episode = new Episode(this, localInfo.getTitle(), localInfo.getSeason(), localInfo.getEpisode(), localInfo.getEndEpisode());

        children.add(new MediaItem(getMediaTree(), localInfo.getUri(), episode));
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return getTitle();
  }
}
