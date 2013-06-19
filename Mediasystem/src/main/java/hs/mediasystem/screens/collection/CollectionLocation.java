package hs.mediasystem.screens.collection;

import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.screens.Location;

public class CollectionLocation implements Location {
  private final MediaRoot mediaRoot;

  public CollectionLocation(MediaRoot mediaRoot) {
    this.mediaRoot = mediaRoot;
  }

  @Override
  public String getId() {
    return mediaRoot.getId().toString();
  }

  @Override
  public Class<?> getParameterType() {
    return mediaRoot.getClass();
  }

  @Override
  public Type getType() {
    return Type.NORMAL;
  }

  public MediaRoot getMediaRoot() {
    return mediaRoot;
  }

  @Override
  public Location getParent() {
    MediaRoot parent = mediaRoot.getParent();

    return parent == null ? null : new CollectionLocation(parent);
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
