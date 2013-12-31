package hs.mediasystem.entity;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

public class Casting extends Entity {
  public final StringProperty role = stringProperty("role");
  public final StringProperty characterName = stringProperty("characterName");
  public final IntegerProperty index = integerProperty();

  public final ObjectProperty<Actor> person = object("person");
  public final ObjectProperty<Media> media = object("media");

  public Casting(Media media, Actor person, String role, String characterName, int index) {
    this.media.set(media);
    this.person.set(person);
    this.role.set(role);
    this.characterName.set(characterName);
    this.index.set(index);
  }
}
