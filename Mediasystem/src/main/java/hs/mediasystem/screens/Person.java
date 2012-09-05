package hs.mediasystem.screens;

import hs.mediasystem.util.Entity;

import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

public class Person extends Entity<Person> {
  public final StringProperty name = string();
  public final StringProperty birthPlace = string();
  public final StringProperty biography = string();
  public final ObjectProperty<Date> birthDate = object();
  public final ObjectProperty<Image> photo = object();  // TODO figure out how to make this work with photoURL

  public final ObjectProperty<ObservableList<Casting>> castings = list("castings");
}
