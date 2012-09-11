package hs.mediasystem.ext.media.movie;

import hs.mediasystem.dao.Item;
import hs.mediasystem.framework.Media;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class Movie extends Media<Movie> {
  public final ObjectProperty<Integer> sequence = new SimpleObjectProperty<>();
  public final StringProperty language = string();
  public final StringProperty tagLine = string();
  public final StringProperty imdbNumber = string();
  public final StringProperty groupTitle = string();

  public Movie(String groupTitle, Integer sequence, String subtitle, Integer releaseYear, String imdb) {
    super(createTitle(groupTitle, sequence), subtitle, releaseYear);

    this.groupTitle.set(groupTitle);
    this.sequence.set(sequence);
    this.imdbNumber.set(imdb);

    item.addListener(new ChangeListener<Item>() {
      @Override
      public void changed(ObservableValue<? extends Item> observableValue, Item old, Item current) {
        language.set(current.getLanguage());
        tagLine.set(current.getTagline());
        imdbNumber.set(current.getImdbId());
      }
    });
  }

  private static String createTitle(String groupTitle, Integer sequence) {
    return groupTitle + (sequence == null || sequence <= 1 ? "" : " " + sequence);
  }
}
