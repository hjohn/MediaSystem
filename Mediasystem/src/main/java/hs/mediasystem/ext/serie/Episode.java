package hs.mediasystem.ext.serie;

import hs.mediasystem.framework.Media;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Episode extends Media {
  private final ObjectProperty<SerieItem> serie = new SimpleObjectProperty<>();
  public SerieItem getSerie() { return serie.get(); }
  public ObjectProperty<SerieItem> serieProperty() { return serie; }

  private final ObjectProperty<Integer> season = new SimpleObjectProperty<>();
  public Integer getSeason() { return season.get(); }
  public ObjectProperty<Integer> seasonProperty() { return season; }

  private final ObjectProperty<Integer> episode = new SimpleObjectProperty<>();
  public Integer getEpisode() { return episode.get(); }
  public ObjectProperty<Integer> episodeProperty() { return episode; }

  private final ObjectProperty<Integer> endEpisode = new SimpleObjectProperty<>();
  public Integer getEndEpisode() { return endEpisode.get(); }
  public ObjectProperty<Integer> endEpisodeProperty() { return endEpisode; }

  private final StringProperty episodeRange = new SimpleStringProperty();
  public String getEpisodeRange() { return episodeRange.get(); }
  public StringProperty episodeRangeProperty() { return episodeRange; }

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
}
