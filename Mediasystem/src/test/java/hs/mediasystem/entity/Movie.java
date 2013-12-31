package hs.mediasystem.entity;

import hs.mediasystem.framework.Media;
import javafx.beans.property.StringProperty;

public class Movie extends Media {
  public final StringProperty uri = stringProperty("uri");
  public final StringProperty name = stringProperty("name");
  public final StringProperty year = stringProperty("year");
  public final StringProperty id = stringProperty("id");
  public final StringProperty season = stringProperty("season");
  public final StringProperty episode = stringProperty("episode");
}
