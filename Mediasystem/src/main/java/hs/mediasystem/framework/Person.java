package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import hs.mediasystem.entity.SimpleEntityProperty;
import hs.mediasystem.util.ImageHandle;

import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Person extends Entity<Person> {
  public final StringProperty name = stringProperty();
  public final StringProperty birthPlace = stringProperty();
  public final StringProperty biography = stringProperty();
  public final ObjectProperty<Date> birthDate = object("birthDate");
  public final ObjectProperty<ImageHandle> photo = object("photo");

  public final SimpleEntityProperty<ObservableList<Casting>> castings = entity("castings");

  public Person(String name, String birthPlace, String biography, Date birthDate, ImageHandle photo) {
    this.name.set(name);
    this.birthPlace.set(birthPlace);
    this.biography.set(biography);
    this.birthDate.set(birthDate);
    this.photo.set(photo);
  }
}
