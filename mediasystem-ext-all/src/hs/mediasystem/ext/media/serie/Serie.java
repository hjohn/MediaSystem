package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.LocalInfo;
import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.framework.EpisodeScanner;
import hs.mediasystem.framework.FileEntitySource;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.descriptors.EntityDescriptors;
import hs.mediasystem.framework.descriptors.AbstractEntityDescriptors;
import hs.mediasystem.framework.descriptors.Descriptor;
import hs.mediasystem.framework.descriptors.DescriptorSet;
import hs.mediasystem.framework.descriptors.DescriptorSet.Attribute;
import hs.mediasystem.framework.MediaRoot;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class Serie extends Media implements MediaRoot {
  private static final EntityDescriptors DESCRIPTORS = new AbstractEntityDescriptors() {{
    Descriptor titleProperty = new Descriptor("title", 0.99999, new TextType(TextType.SubType.STRING, TextType.Size.TITLE));

    initializeDescriptors(
      titleProperty,
      new Descriptor("media.image", 0.99999, new ImageType(), titleProperty)
    );

    initializeSets(
      new DescriptorSet(
        ImmutableList.of(getDescriptor("title")),
        ImmutableSet.of(Attribute.PREFERRED, Attribute.SORTABLE, Attribute.CONCISE)
      )
    );
  }};

  private final SeriesMediaTree mediaRoot;
  private final Id id;
  private final FileEntitySource fileEntitySource;

  private List<Media> children;

  public Serie(SeriesMediaTree mediaTree, MediaItem mediaItem, FileEntitySource fileEntitySource) {
    super(DESCRIPTORS, mediaItem);

    this.id = new Id("serie");
    this.mediaRoot = mediaTree;
    this.fileEntitySource = fileEntitySource;
  }

  public Serie() {
    this(null, null, null);
  }

  @Override
  public List<? extends Media> getItems() {
    if(children == null) {
      List<LocalInfo> scanResults = new EpisodeScanner(new EpisodeDecoder(title.get()), 2).scan(Paths.get(getMediaItem().getUri()));

      children = new ArrayList<>();

      for(LocalInfo localInfo : scanResults) {
        Episode episode = getContext().add(Episode.class, new Supplier<Episode>() {
          @Override
          public Episode get() {
            Episode episode = new Episode(new MediaItem(localInfo.getUri()));

            episode.serie.set(Serie.this);
            episode.season.set(localInfo.getSeason());
            episode.episode.set(localInfo.getEpisode());
            episode.endEpisode.set(localInfo.getEndEpisode());

            return episode;
          }
        }, new SourceKey(fileEntitySource, localInfo.getUri()));

        children.add(episode);
      }
    }

    return children;
  }

  @Override
  public String getRootName() {
    return title.get();
  }

  @Override
  public Id getId() {
    return id;
  }

  @Override
  public MediaRoot getParent() {
    return mediaRoot;
  }
  
  @Override
  public Map<String, Object> getMediaProperties() {
    return new HashMap<>();
  }
}
