package hs.mediasystem.screens;

import javafx.scene.paint.Color;
import hs.mediasystem.util.Location;

public class MainScreenLocation implements Location {

  @Override
  public String getId() {
    return "MainScreen";
  }

  @Override
  public Class<?> getParameterType() {
    return null;
  }

  @Override
  public Color getBackgroundColor() {
    return Color.BLACK;
  }

  @Override
  public Location getParent() {
    return null;
  }

  @Override
  public String getBreadCrumb() {
    return "Home";
  }
}
