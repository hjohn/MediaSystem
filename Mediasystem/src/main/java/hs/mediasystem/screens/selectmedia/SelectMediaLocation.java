package hs.mediasystem.screens.selectmedia;

import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.util.Location;
import javafx.scene.paint.Color;

public class SelectMediaLocation implements Location {
  private final MediaRoot mediaRoot;

  public SelectMediaLocation(MediaRoot mediaRoot) {
    this.mediaRoot = mediaRoot;
  }

  @Override
  public String getId() {
    return mediaRoot.getId();
  }

  @Override
  public Class<?> getParameterType() {
    return mediaRoot.getClass();
  }

  @Override
  public Color getBackgroundColor() {
    return Color.BLACK;
  }

  public MediaRoot getMediaRoot() {
    return mediaRoot;
  }

  @Override
  public Location getParent() {
    MediaRoot parent = mediaRoot.getParent();

    return parent == null ? null : new SelectMediaLocation(parent);
  }

  @Override
  public String getBreadCrumb() {
    StringBuilder sb = new StringBuilder();

    MediaRoot parent = mediaRoot;

    while(parent != null) {
      sb.insert(0, " > " + parent.getRootName());

      parent = parent.getParent();
    }

    sb.insert(0, "Home");

    return sb.toString();
  }
}
