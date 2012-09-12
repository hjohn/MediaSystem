package hs.mediasystem.ext.media.serie;

import hs.mediasystem.dao.Item;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.SourceImageHandle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Episode extends Media<Episode> {
  public final ObjectProperty<SerieItem> serie = object("serie");
  public final ObjectProperty<Integer> season = object("season");
  public final ObjectProperty<Integer> episode = object("episode");
  public final ObjectProperty<Integer> endEpisode = object("endEpisode");
  public final StringProperty episodeRange = string();

  public Episode(final SerieItem serie, String episodeName, final Integer season, final Integer episode, final Integer endEpisode) {
    super(episodeName == null ? createTitle(serie, season, episode, endEpisode) : episodeName);

    assert serie != null;
    assert (season != null && episode != null && endEpisode != null) || episodeName != null;

    this.serie.set(serie);
    this.season.set(season);
    this.episode.set(episode);
    this.endEpisode.set(endEpisode);
    this.episodeRange.set(createEpisodeNumber(episode, endEpisode));

    item.addListener(new ChangeListener<Item>() {
      @Override
      public void changed(ObservableValue<? extends Item> observableValue, Item old, Item current) {
        background.set(current.getBackground() == null ? serie.getMedia().background.get() : new SourceImageHandle(current.getBackground(), "Episode:/" + createTitle(serie, season, episode, endEpisode)));
      }
    });
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
