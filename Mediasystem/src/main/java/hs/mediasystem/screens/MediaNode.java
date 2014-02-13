package hs.mediasystem.screens;

import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class MediaNode {
  public final ObjectProperty<MediaData> mediaData;
  public final ObjectProperty<Media> media;

  public final Media getMedia() { return media.get(); }

  private final String id;
  private final String shortTitle;
  private final boolean isLeaf;

  private final List<MediaNode> children;

  private MediaNode parent;

  public MediaNode(Media media) {
    assert media.getMediaItem() != null;

    this.id = "mediaItem[" + media.getMediaItem().getUri() + "]";
    this.media = new SimpleObjectProperty<>(media);
    this.mediaData = media.getMediaItem().mediaData;
    this.shortTitle = "";
    this.isLeaf = true;
    this.children = new ArrayList<>();
  }

  public MediaNode(String id, String title, String shortTitle, boolean isLeaf, List<MediaNode> children) {
    Media media = new SpecialItem(title);

    this.id = id;
    this.media = new SimpleObjectProperty<>(media);
    this.mediaData = new SimpleObjectProperty<>(this, "mediaData");
    this.shortTitle = shortTitle == null ? media.title.get() : shortTitle;
    this.isLeaf = isLeaf;
    this.children = children == null ? new ArrayList<>() : new ArrayList<>(children);
  }

  public MediaNode(String id, String title, String shortTitle, boolean isLeaf) {
    this(id, title, shortTitle, isLeaf, null);
  }

  public String getId() {
    return id;
  }

  public MediaNode getParent() {
    return parent;
  }

  public String getShortTitle() {
    return shortTitle;
  }

  public void add(MediaNode child) {
    if(child.parent != null) {
      throw new IllegalStateException("cannot add child twice: " + child);
    }

    child.parent = this;
    children.add(child);
  }

  public MediaNode findMediaNodeById(String id) {
    if(this.id.equalsIgnoreCase(id)) {
      return this;
    }

    for(MediaNode node : children) {
      MediaNode matchingNode = node.findMediaNodeById(id);

      if(matchingNode != null) {
        return matchingNode;
      }
    }

    return null;
  }

  public List<MediaNode> getChildren() {
    return Collections.unmodifiableList(children);
  }

  /**
   * Returns <code>true</code> if this MediaNode is a leaf node.  Leaf nodes are either points that cause an action to be taken
   * (like playing a media) or that cause navigation to occur to a new display or layout.
   *
   * @return <code>true</code> if this MediaNode is a leaf node
   */
  public boolean isLeaf() {
    return isLeaf;
  }

  @Override
  public String toString() {
    return "MediaNode[id='" + id + "', instance=" + hashCode() + "]";
  }

  public static class SpecialItem extends Media {
    public SpecialItem(String rootName) {
      localTitle.set(rootName);
    }
  }
}
