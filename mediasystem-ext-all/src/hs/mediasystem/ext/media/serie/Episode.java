package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.Media;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class Episode extends Media {
  public final ObjectProperty<Serie> serie = object("serie");
  public final ObjectProperty<Integer> season = object("season");
  public final ObjectProperty<Integer> episode = object("episode");
  public final ObjectProperty<Integer> endEpisode = object("endEpisode");
  public final StringProperty episodeRange = stringProperty("episodeRange");

  public Episode setAll(final Serie serie, String episodeName, final Integer season, final Integer episode, final Integer endEpisode) {
    setTitle(episodeName == null ? createTitle(serie, season, episode, endEpisode) : episodeName);

    assert serie != null;
    assert (season != null && episode != null && endEpisode != null) || episodeName != null;

    this.serie.set(serie);
    this.season.set(season);
    this.episode.set(episode);
    this.endEpisode.set(endEpisode);
    this.episodeRange.set(createEpisodeNumber(episode, endEpisode));
    this.titleWithContext.set(createTitle(serie, season, episode, endEpisode));

    return this;
  }

  private static String createTitle(Serie serie, Integer season, Integer episode, Integer endEpisode) {
    return serie.title.get() + " " + season + "x" + createEpisodeNumber(episode, endEpisode);
  }

  private static String createEpisodeNumber(Integer episode, Integer endEpisode) {
    return episode == null ? null : ("" + episode + (endEpisode != episode ? "-" + endEpisode : ""));
  }

  @Override
  public String toString() {
    return "Episode('" + title.get() + "')";
  }
}
