package hs.mediasystem.entity;

import hs.mediasystem.framework.Media;
import javafx.beans.property.StringProperty;

public class Movie extends Media<Movie> {
  public final StringProperty uri = string();
  public final StringProperty name = string();
  public final StringProperty year = string();
  public final StringProperty id = string();
  public final StringProperty season = string();
  public final StringProperty episode = string();
}
