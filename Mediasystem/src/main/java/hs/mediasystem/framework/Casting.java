package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class Casting extends Entity {
  public final StringProperty role = stringProperty("role");
  public final StringProperty characterName = stringProperty("characterName");
  public final IntegerProperty index = integerProperty("index");

  public final ObjectProperty<Person> person = object("person");
  public final ObjectProperty<Media> media = object("media");
}
