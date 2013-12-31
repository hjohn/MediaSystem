package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.Media;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

public class Movie extends Media {
  public final ObjectProperty<Integer> sequence = new SimpleObjectProperty<>();
  public final StringProperty language = stringProperty("language");
  public final StringProperty tagLine = stringProperty("tagLine");
  public final StringProperty imdbNumber = stringProperty("imdbNumber");
  public final StringProperty groupTitle = stringProperty("groupTitle");

  public Movie setAll(String groupTitle, Integer sequence, String subtitle, String imdb) {
    setTitles(createTitle(groupTitle, sequence), subtitle);

    this.groupTitle.set(groupTitle);
    this.sequence.set(sequence);
    this.imdbNumber.set(imdb);

    return this;
  }

  private static String createTitle(String groupTitle, Integer sequence) {
    return groupTitle + (sequence == null || sequence <= 1 ? "" : " " + sequence);
  }
}
