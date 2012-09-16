package hs.mediasystem.entity;

import hs.mediasystem.framework.Media;
import javafx.beans.property.StringProperty;

public class Movie extends Media<Movie> {
  public final StringProperty uri = stringProperty();
  public final StringProperty name = stringProperty();
  public final StringProperty year = stringProperty();
  public final StringProperty id = stringProperty();
  public final StringProperty season = stringProperty();
  public final StringProperty episode = stringProperty();
}
