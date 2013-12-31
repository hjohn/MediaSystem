package hs.mediasystem.framework;

import hs.mediasystem.entity.Entity;
import hs.mediasystem.util.ImageHandle;

import java.time.LocalDate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

public class Person extends Entity {
  public final StringProperty name = stringProperty("name");
  public final StringProperty birthPlace = stringProperty("birthPlace");
  public final StringProperty biography = stringProperty("biography");
  public final ObjectProperty<LocalDate> birthDate = object("birthDate");
  public final ObjectProperty<ImageHandle> photo = object("photo");

  public final ObjectProperty<ObservableList<Casting>> castings = list(Casting.class);
}
