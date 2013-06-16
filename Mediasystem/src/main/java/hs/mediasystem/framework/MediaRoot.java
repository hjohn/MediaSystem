package hs.mediasystem.framework;

import java.util.List;
import java.util.Map;

public interface MediaRoot {
  String getId();
  String getRootName();
  List<? extends MediaItem> getItems();
  MediaRoot getParent();
  Map<String, Object> getMediaProperties();
}
