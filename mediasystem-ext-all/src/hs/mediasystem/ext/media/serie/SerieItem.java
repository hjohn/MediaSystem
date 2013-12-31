package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.framework.EpisodeScanner;
import hs.mediasystem.framework.FileEntitySource;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class SerieItem extends MediaItem implements MediaRoot {
  private final SeriesMediaTree mediaRoot;
  private final Id id;
  private final FileEntitySource fileEntitySource;

  private List<MediaItem> children;


  public SerieItem(SeriesMediaTree mediaTree, String uri, String serieTitle, FileEntitySource fileEntitySource) {
    super(uri, serieTitle, Serie.class);

    this.id = new Id("serie");
    this.mediaRoot = mediaTree;
    this.fileEntitySource = fileEntitySource;
  }

  @Override
  public List<? extends MediaItem> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new EpisodeScanner(new EpisodeDecoder(getTitle()), 2).scan(Paths.get(getUri()));

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        MediaItem mediaItem = getContext().add(MediaItem.class, new Supplier<MediaItem>() {
          @Override
          public MediaItem get() {
            return new MediaItem(localInfo.getUri(), getMedia().title.get(), null, localInfo.getSeason() + "x" + localInfo.getEpisode(), null, Episode.class);
          }
        }, new SourceKey(fileEntitySource, localInfo.getUri()));

        mediaItem.properties.put("serie", this);
        mediaItem.properties.put("season", localInfo.getSeason());
        mediaItem.properties.put("episodeNumber", localInfo.getEpisode());
        mediaItem.properties.put("endEpisode", localInfo.getEndEpisode());
        mediaItem.properties.put("episodeRange", localInfo.getEpisode() == null ? null : ("" + localInfo.getEpisode() + (localInfo.getEndEpisode() != localInfo.getEpisode() ? "-" + localInfo.getEndEpisode() : "")));

        children.add(mediaItem);
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return getTitle();
  }

  @Override
  public Id getId() {
    return id;
  }

  @Override
  public MediaRoot getParent() {
    return mediaRoot;
  }

  private static final Map<String, Object> MEDIA_PROPERTIES = new HashMap<>();

  static {
    MEDIA_PROPERTIES.put("image.poster", null);
    MEDIA_PROPERTIES.put("image.poster.aspectRatios", new double[] {16.0 / 9.0, 4.0 / 3.0});
    MEDIA_PROPERTIES.put("image.poster.hasIdentifyingTitle", false);

    MEDIA_PROPERTIES.put("image.background", null);
    MEDIA_PROPERTIES.put("image.background.aspectRatios", new double[] {2.0 / 3.0, 4.0 / 3.0});
    MEDIA_PROPERTIES.put("image.background.hasIdentifyingTitle", false);
    MEDIA_PROPERTIES.put("image.background.fromParent", true);

    MEDIA_PROPERTIES.put("image.banner", null);
    MEDIA_PROPERTIES.put("image.banner.aspectRatios", new double[] {6.0 / 1.0});
    MEDIA_PROPERTIES.put("image.banner.hasIdentifyingTitle", true);
    MEDIA_PROPERTIES.put("image.banner.fromParent", true);
  }

  @Override
  public Map<String, Object> getMediaProperties() {
    return Collections.unmodifiableMap(MEDIA_PROPERTIES);
  }
}
