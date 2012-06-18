package hs.mediasystem.ext.media.movie;

import hs.mediasystem.framework.Media;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Movie extends Media {
  private final ObjectProperty<Integer> sequence = new SimpleObjectProperty<>();
  public Integer getSequence() { return sequence.get(); }
  public ObjectProperty<Integer> sequenceProperty() { return sequence; }

  private final StringProperty language = new SimpleStringProperty() {
    @Override
    public String get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public String getLanguage() { return language.get(); }
  public StringProperty languageProperty() { return language; }

  private final StringProperty tagLine = new SimpleStringProperty() {
    @Override
    public String get() {
      queueForEnrichment();
      return super.get();
    }
  };
  public String getTagLine() { return tagLine.get(); }
  public StringProperty tagLineProperty() { return tagLine; }

  private final StringProperty imdbNumber = new SimpleStringProperty();
  public String getImdbNumber() { return imdbNumber.get(); }
  public StringProperty imdbNumberProperty() { return imdbNumber; }

  private final StringProperty groupTitle = new SimpleStringProperty();
  public String getGroupTitle() { return groupTitle.get(); }
  public StringProperty groupTitleProperty() { return groupTitle; }

  public Movie(String groupTitle, Integer sequence, String subtitle, Integer releaseYear, String imdbNumber) {
    super(createTitle(groupTitle, sequence), subtitle, releaseYear);
    this.groupTitle.set(groupTitle);
    this.sequence.set(sequence);
    this.imdbNumber.set(imdbNumber);
  }

  private static String createTitle(String groupTitle, Integer sequence) {
    return groupTitle + (sequence == null || sequence <= 1 ? "" : " " + sequence);
  }
}
