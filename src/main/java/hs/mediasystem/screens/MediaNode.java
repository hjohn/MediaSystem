package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;

import java.util.Collections;
import java.util.List;

public class MediaNode {
  private final StandardLayout layout;
  private final MediaItem mediaItem;
  private final String id;

  private MediaNode parent;
  private List<MediaNode> children;

  public MediaNode(StandardLayout layout, MediaItem mediaItem) {
    assert mediaItem != null;

    this.layout = layout;
    this.mediaItem = mediaItem;
    this.id = mediaItem.getUri() == null ? mediaItem.getLocalInfo().getTitle() : mediaItem.getUri();

    assert this.id != null;
  }

  public String getId() {
    return id;
  }

  public MediaNode getParent() {
    return parent;
  }

  public void setChildren(List<MediaNode> children) {
    for(MediaNode child : children) {
      if(child.parent != null) {
        throw new IllegalStateException("cannot add child twice: " + child);
      }

      child.parent = this;
    }

    this.children = children;
  }

  public MediaNode findMediaNode(String id) {
    for(MediaNode node : getChildren()) {
      if(node.getId().equalsIgnoreCase(id)) {
        return node;
      }
      else if(!node.isLeaf()) {
        MediaNode childNode = node.findMediaNode(id);

        if(childNode != null) {
          return childNode;
        }
      }
    }

    return null;
  }

  public MediaItem getMediaItem() {
    return mediaItem;
  }

  public List<MediaNode> getChildren() {
    if(children == null) {
      setChildren(layout.getChildren(this));
    }

    return Collections.unmodifiableList(children);
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

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null || getClass() != obj.getClass()) {
      return false;
    }

    MediaNode other = (MediaNode) obj;

    return id.equals(other.id);
  }
}
