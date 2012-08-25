package hs.mediasystem.util;

import javafx.scene.paint.Color;

public interface Location {
  String getId();
  String getBreadCrumb();
  Location getParent();
  Class<?> getParameterType();
  Color getBackgroundColor();
}
