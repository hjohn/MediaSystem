package hs.mediasystem.framework;

import java.util.List;

public interface MediaRoot {
  List<? extends MediaItem> getItems();
}
