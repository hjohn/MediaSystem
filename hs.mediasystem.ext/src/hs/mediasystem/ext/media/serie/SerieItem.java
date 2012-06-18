package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.MediaTree;
import hs.mediasystem.fs.EpisodeDecoder;
import hs.mediasystem.fs.EpisodeScanner;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SerieItem extends MediaItem implements MediaRoot {
  private List<MediaItem> children;

  public SerieItem(MediaTree mediaTree, String uri, SerieBase serie) {
    super(mediaTree, uri, serie);
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new EpisodeScanner(new EpisodeDecoder(getTitle())).scan(Paths.get(getUri()));

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        EpisodeBase episode = new EpisodeBase(this, getTitle() + " " + localInfo.getSeason() + "x" + localInfo.getEpisode(), localInfo.getSeason(), localInfo.getEpisode(), localInfo.getEndEpisode());

        children.add(new MediaItem(getMediaTree(), localInfo.getUri(), episode));
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return getTitle();
  }

  @Override
  public String getId() {
    return "serie[" + getTitle() + "]";
  }
}
