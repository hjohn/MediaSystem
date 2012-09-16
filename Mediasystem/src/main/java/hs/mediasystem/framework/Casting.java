package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class Casting extends Entity<Casting> {
  public final StringProperty role = stringProperty();
  public final StringProperty characterName = stringProperty();
  public final IntegerProperty index = integerProperty();

  public final ObjectProperty<Person> person = object("person");
  public final ObjectProperty<Media<?>> media = object("media");
}
