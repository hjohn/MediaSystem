package hs.mediasystem.framework;

import java.util.List;

public interface MediaRoot {

  /**
   * Returns an Id which identifies this type of MediaRoot.
   *
   * @return an Id which identifies this type of MediaRoot
   */
  Id getId();

  String getRootName();
  List<? extends Media> getItems();
  MediaRoot getParent();
}
