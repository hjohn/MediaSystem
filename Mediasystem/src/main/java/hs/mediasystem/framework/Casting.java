package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class Casting extends Entity {
  public enum MediaType {TV, MOVIE}

  public final StringProperty role = stringProperty("role");  // Currently only "Actor", "Guest Star" or "Self"
  public final StringProperty characterName = stringProperty("characterName");   // Can be empty
  public final IntegerProperty index = integerProperty("index");
  public final ObjectProperty<MediaType> mediaType = object("mediaType");
  public final IntegerProperty episodeCount = integerProperty("episodeCount");

  public final ObjectProperty<Person> person = object("person");
  public final ObjectProperty<Media> media = object("media");
}
