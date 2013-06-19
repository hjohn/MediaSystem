package hs.mediasystem.framework;

import java.util.List;
import java.util.Map;

public interface MediaRoot {

  /**
   * Returns an Id which identifies this type of MediaRoot.
   *
   * @return an Id which identifies this type of MediaRoot
   */
  Id getId();

  String getRootName();
  List<? extends MediaItem> getItems();
  MediaRoot getParent();
  Map<String, Object> getMediaProperties();
}
