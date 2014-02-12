package hs.mediasystem.entity;

import hs.mediasystem.entity.Entity;

import java.time.LocalDate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Actor extends Entity {
  public final StringProperty name = stringProperty("name");
  public final StringProperty birthPlace = stringProperty("birthPlace");
  public final StringProperty biography = stringProperty("biography");
  public final ObjectProperty<LocalDate> birthDate = object("birthDate");
  public final ObjectProperty<Actor> father = object("father");

  public final ObjectProperty<ObservableList<Casting>> castings = list("castings", Casting.class);

  public Actor(String name) {
    this.name.set(name);
  }

  public Actor() {
  }

  @Override
  public String toString() {
    return "Person['" + name.get() + "']";
  }
}
