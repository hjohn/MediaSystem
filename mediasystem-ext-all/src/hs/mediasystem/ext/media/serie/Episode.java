package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.descriptors.Descriptor;
import hs.mediasystem.framework.descriptors.DescriptorSet;
import hs.mediasystem.framework.descriptors.DescriptorSet.Attribute;
import hs.mediasystem.framework.descriptors.EntityDescriptors;
import hs.mediasystem.framework.descriptors.EntityDescriptors.ImageType;
import hs.mediasystem.framework.descriptors.EntityDescriptors.ObjectType;
import hs.mediasystem.framework.descriptors.EntityDescriptors.TextType;
import hs.mediasystem.framework.descriptors.StaticEntityDescriptors;
import hs.mediasystem.util.MapBindings;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class Episode extends Media {
  public static final EntityDescriptors DESCRIPTORS = new StaticEntityDescriptors(Properties.class);

  static class Properties {
    public static final Descriptor TITLE = new Descriptor("title", 0.99999, new TextType(TextType.SubType.STRING, TextType.Size.TITLE));
    public static final Descriptor SERIE = new Descriptor("serie", 0.99999, new ObjectType());
    public static final Descriptor SEASON = new Descriptor("season", 0.8, new TextType(TextType.SubType.STRING, TextType.Size.WORD));
    public static final Descriptor EPISODE_RANGE = new Descriptor("episodeRange", 0.95, new TextType(TextType.SubType.STRING, TextType.Size.WORD));
    public static final Descriptor IMAGE = new Descriptor("image", 0.99999, new ImageType());
    public static final Descriptor SEASON_AND_EPISODE = new Descriptor("seasonAndEpisode", new TextType(TextType.SubType.STRING, TextType.Size.WORD), SEASON, EPISODE_RANGE);

    public static final DescriptorSet SET1 = new DescriptorSet(
      ImmutableList.of(SERIE, TITLE),
      ImmutableSet.of(Attribute.PREFERRED)
    );

    public static final DescriptorSet SET2 = new DescriptorSet(
      ImmutableList.of(SERIE, SEASON, EPISODE_RANGE),
      ImmutableSet.of(Attribute.SORTABLE, Attribute.CONCISE)
    );
  }

  public final ObjectProperty<Serie> serie = object("serie");

  public final ObjectProperty<Integer> season = object("season");
  public final ObjectProperty<Integer> episode = object("episode");
  public final ObjectProperty<Integer> endEpisode = object("endEpisode");
  public final StringProperty episodeRange = stringProperty("episodeRange");
  public final StringProperty seasonAndEpisode = stringProperty("seasonAndEpisode");

  public Episode(MediaItem mediaItem) {
    super(DESCRIPTORS, mediaItem);

    this.episodeRange.bind(Bindings.when(this.endEpisode.isEqualTo(this.episode)).then(this.episode.asString()).otherwise(Bindings.concat(this.episode, "-", this.endEpisode)));
    this.seasonAndEpisode.bind(Bindings.concat(season, "x", episodeRange));

    this.titleWithContext.bind(Bindings.concat(MapBindings.selectString(serie, "title"), " ", this.season, "x", this.episodeRange));
  }

  public Episode() {
    this(null);
  }

  @Override
  public String toString() {
    return "Episode('" + title.get() + "')";
  }
}
