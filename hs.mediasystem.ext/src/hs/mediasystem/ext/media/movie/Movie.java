package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.Media;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

public class Movie extends Media<Movie> {
  public final ObjectProperty<Integer> sequence = new SimpleObjectProperty<>();
  public final StringProperty language = stringProperty();
  public final StringProperty tagLine = stringProperty();
  public final StringProperty imdbNumber = stringProperty();
  public final StringProperty groupTitle = stringProperty();

  public Movie(String groupTitle, Integer sequence, String subtitle, Integer releaseYear, String imdb) {
    super(createTitle(groupTitle, sequence), subtitle, releaseYear);

    this.groupTitle.set(groupTitle);
    this.sequence.set(sequence);
    this.imdbNumber.set(imdb);
  }

  private static String createTitle(String groupTitle, Integer sequence) {
    return groupTitle + (sequence == null || sequence <= 1 ? "" : " " + sequence);
  }
}
