package hs.mediasystem.framework;

import java.util.List;

public interface MediaRoot {
  String getId();
  String getRootName();
  List<? extends MediaItem> getItems();
  MediaRoot getParent();
}
