package hs.mediasystem.framework;

import java.util.List;

public interface MediaRoot {
  String getRootName();
  List<? extends MediaItem> getItems();
}
