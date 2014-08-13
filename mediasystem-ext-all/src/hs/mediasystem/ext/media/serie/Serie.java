package hs.mediasystem.ext.media.serie;

import hs.mediasystem.entity.SourceKey;
import hs.mediasystem.ext.media.serie.Episode.SpecialPosition;
import hs.mediasystem.ext.media.serie.Episode.Type;
import hs.mediasystem.framework.EpisodeScanner;
import hs.mediasystem.framework.FileEntitySource;
import hs.mediasystem.framework.Id;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.framework.NameDecoder;
import hs.mediasystem.framework.NameDecoder.DecodeResult;
import hs.mediasystem.framework.NameDecoder.Hint;
import hs.mediasystem.framework.descriptors.AbstractEntityDescriptors;
import hs.mediasystem.framework.descriptors.Descriptor;
import hs.mediasystem.framework.descriptors.DescriptorSet;
import hs.mediasystem.framework.descriptors.DescriptorSet.Attribute;
import hs.mediasystem.framework.descriptors.EntityDescriptors;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

  private static final Pattern SEASON_EPISODE_PATTERN = Pattern.compile("(?:([0-9]+)(?:,([0-9]+)(?:-([0-9]+))?([ab])?)?)?");
  private static final NameDecoder NAME_DECODER = new NameDecoder(Hint.EPISODE, Hint.MOVIE);

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
      List<Path> scanResults = new EpisodeScanner(2).scan(Paths.get(getMediaItem().getUri()));

      children = new ArrayList<>();

      for(Path path : scanResults) {
        DecodeResult result = NAME_DECODER.decode(path.getFileName().toString());
        Matcher seasonEpisodeMatcher = SEASON_EPISODE_PATTERN.matcher(result.getSequence() == null ? "" : result.getSequence());

        if(seasonEpisodeMatcher.matches()) {  // Should always match
          String title = result.getTitle();
          String subtitle = result.getSubtitle();
          Integer season = seasonEpisodeMatcher.group(1) == null ? null : Integer.valueOf(seasonEpisodeMatcher.group(1));
          Integer episode = seasonEpisodeMatcher.group(2) == null ? null : Integer.valueOf(seasonEpisodeMatcher.group(2));
          Integer endEpisode = seasonEpisodeMatcher.group(3) == null ? episode : Integer.valueOf(seasonEpisodeMatcher.group(3));
          SpecialPosition specialPosition = seasonEpisodeMatcher.group(4) == null ? null : seasonEpisodeMatcher.group(4).equals("a") ? SpecialPosition.AFTER : SpecialPosition.BEFORE;
          Type type = specialPosition != null ? Type.SPECIAL :
                              episode != null ? Type.EPISODE :
                                                Type.OTHER;

          Episode item = getContext().add(Episode.class, new Supplier<Episode>() {
            @Override
            public Episode get() {
              Episode item = new Episode(new MediaItem(path.toString()));

              item.serie.set(Serie.this);
              item.type.set(type);
              item.specialPosition.set(specialPosition);
              item.season.set(season);
              item.episode.set(episode);
              item.endEpisode.set(endEpisode);

              switch(type) {
              case SPECIAL:
              case EPISODE:
                item.initialTitle.set(subtitle != null ? subtitle : title);
                break;
              case OTHER:
                item.initialTitle.set(title);
                item.subtitle.set(subtitle);
                break;
              }

              return item;
            }
          }, new SourceKey(fileEntitySource, path.toString()));

          children.add(item);
        }
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
}
