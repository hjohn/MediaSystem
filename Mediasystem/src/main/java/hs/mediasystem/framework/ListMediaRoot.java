package hs.mediasystem.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MediaRoot implementation that stores its MediaItem objects in a simple list.
 */
public class ListMediaRoot implements MediaRoot {
  private final List<MediaItem> items = new ArrayList<>();
  private final MediaRoot parent;
  private final Id id;
  private final String rootName;

  public ListMediaRoot(MediaRoot parent, Id id, String rootName) {
    this.parent = parent;
    this.id = id;
    this.rootName = rootName;
  }

  public void add(MediaItem mediaItem) {
    items.add(mediaItem);
  }

  @Override
  public Id getId() {
    return id;
  }

  @Override
  public String getRootName() {
    return rootName;
  }

  @Override
  public List<? extends MediaItem> getItems() {
    return Collections.unmodifiableList(items);
  }

  @Override
  public MediaRoot getParent() {
    return parent;
  }

  private static final Map<String, Object> MEDIA_PROPERTIES = new HashMap<>();

  @Override
  public Map<String, Object> getMediaProperties() {
    return Collections.unmodifiableMap(MEDIA_PROPERTIES);
  }
}
