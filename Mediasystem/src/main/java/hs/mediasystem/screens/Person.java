package hs.mediasystem.screens;

import hs.mediasystem.util.Entity;
import hs.mediasystem.util.ImageHandle;

import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Person extends Entity<Person> {
  public final StringProperty name = string();
  public final StringProperty birthPlace = string();
  public final StringProperty biography = string();
  public final ObjectProperty<Date> birthDate = object();
  public final ObjectProperty<ImageHandle> photo = object();

  public final ObjectProperty<ObservableList<Casting>> castings = list("castings");
}
