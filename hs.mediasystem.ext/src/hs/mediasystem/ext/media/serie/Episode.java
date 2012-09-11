package hs.mediasystem.ext.media.serie;

import hs.mediasystem.framework.Media;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class Episode extends Media<Episode> {
  public final ObjectProperty<SerieItem> serie = object();
  public final ObjectProperty<Integer> season = object();
  public final ObjectProperty<Integer> episode = object();
  public final ObjectProperty<Integer> endEpisode = object();
  public final StringProperty episodeRange = string();

  public Episode(SerieItem serie, String episodeName, Integer season, Integer episode, Integer endEpisode) {
    super(episodeName == null ? createTitle(serie, season, episode, endEpisode) : episodeName);

    assert serie != null;
    assert (season != null && episode != null && endEpisode != null) || episodeName != null;

    this.serie.set(serie);
    this.season.set(season);
    this.episode.set(episode);
    this.endEpisode.set(endEpisode);
    this.episodeRange.set(createEpisodeNumber(episode, endEpisode));
  }

  private static String createTitle(SerieItem serie, Integer season, Integer episode, Integer endEpisode) {
    return serie.getTitle() + " " + season + "x" + createEpisodeNumber(episode, endEpisode);
  }

  private static String createEpisodeNumber(Integer episode, Integer endEpisode) {
    return episode == null ? null : ("" + episode + (endEpisode != episode ? "-" + endEpisode : ""));
  }

  @Override
  public String toString() {
    return "Episode('" + title.get() + "')";
  }
}
