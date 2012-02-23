package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;

import java.util.Collections;
import java.util.List;

public class MediaNode {
  private final StandardLayout layout;
  private final MediaItem mediaItem;
  private final List<MediaNode> children;

  public MediaNode(StandardLayout layout, MediaItem mediaItem, List<MediaNode> children) {
    this.layout = layout;
    this.mediaItem = mediaItem;
    this.children = children;
  }

  public MediaNode(StandardLayout layout, MediaItem mediaItem) {
    this(layout, mediaItem, null);
  }

  public MediaItem getMediaItem() {
    return mediaItem;
  }

  public List<MediaNode> getChildren() {
    return children != null ? Collections.unmodifiableList(children) : layout.getChildren(mediaItem);
  }

  public boolean hasChildren() {
    return children != null ? !children.isEmpty() : layout.hasChildren(mediaItem);
  }

  public boolean isLeaf() {
    return layout.isRoot(mediaItem) || mediaItem.isLeaf();
  }

  public CellProvider<MediaNode> getCellProvider() {
    return layout.getCellProvider(mediaItem);
  }

  public boolean expandTopLevel() {
    return layout.expandTopLevel(mediaItem);
  }
}
