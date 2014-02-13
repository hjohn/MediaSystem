package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.MapBindings;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class Episode extends Media {
  public final ObjectProperty<Serie> serie = object("serie");

  public final IntegerProperty season = integerProperty("season");
  public final IntegerProperty episode = integerProperty("episode");
  public final IntegerProperty endEpisode = integerProperty("endEpisode");
  public final StringProperty episodeRange = stringProperty("episodeRange");
  public final StringProperty seasonAndEpisode = stringProperty("seasonAndEpisode");

  public Episode(MediaItem mediaItem) {
    super(mediaItem);

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
