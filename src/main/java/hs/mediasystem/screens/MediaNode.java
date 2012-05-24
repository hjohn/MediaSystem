package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.media.Media;
import hs.mediasystem.util.MapBindings;

import java.util.Collections;
import java.util.List;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Callback;

public class MediaNode {
  private final StringProperty title = new SimpleStringProperty();
  public String getTitle() { return title.get(); }
  public StringProperty titleProperty() { return title; }

  private final ObjectProperty<Integer> releaseYear = new SimpleObjectProperty<>();
  public Integer getReleaseYear() { return releaseYear.get(); }
  public ObjectProperty<Integer> releaseYearProperty() { return releaseYear; }

  private final ObjectProperty<MediaItem> mediaItem = new SimpleObjectProperty<>();
  public MediaItem getMediaItem() { return mediaItem.get(); }
  public ObjectProperty<MediaItem> mediaItemProperty() { return mediaItem; }

  private final String id;
  private final String shortTitle;
  private final boolean isLeaf;

  private MediaNode parent;
  private List<MediaNode> children;
  private boolean showTopLevelExpanded;

  public MediaNode(MediaItem mediaItem) {
    assert mediaItem != null;

    this.mediaItem.set(mediaItem);
    this.id = mediaItem.getUri() == null ? mediaItem.getMediaType() : mediaItem.getUri();

    assert this.id != null;

    this.title.bind(MapBindings.selectString(mediaItem.dataMapProperty(), Media.class, "title"));
    ObjectBinding<Integer> releaseYearBinding = MapBindings.select(mediaItem.dataMapProperty(), Media.class, "releaseYear");
    this.releaseYear.bind(releaseYearBinding);

    this.shortTitle = "";
    this.isLeaf = mediaItem instanceof MediaRoot || mediaItem.isLeaf();
    this.dataType = mediaItem.getMedia().getClass();
    this.showTopLevelExpanded = false;
  }

  public MediaNode(String title, String shortTitle, Integer releaseYear) {
    this.showTopLevelExpanded = false;
    this.shortTitle = shortTitle == null ? title : shortTitle;
    this.mediaItem.set(null);

    this.title.set(title);
    this.releaseYear.set(releaseYear);

    this.id = "MediaNode://" + title + "/" + releaseYear;

    this.isLeaf = false;
    this.dataType = Media.class;
  }

  private MediaRoot mediaRoot;
  private final Class<?> dataType;
  private Callback<MediaRoot, List<MediaNode>> childrenCallback;

  public MediaNode(MediaRoot mediaRoot, boolean showTopLevelExpanded, Callback<MediaRoot, List<MediaNode>> childrenCallback) {
    this(mediaRoot.getRootName(), null, null);

    this.mediaRoot = mediaRoot;
    this.showTopLevelExpanded = showTopLevelExpanded;
    this.childrenCallback = childrenCallback;
  }

  public MediaRoot getMediaRoot() {
    return mediaRoot;
  }

  public Class<?> getDataType() {
    return dataType;
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

  public int indexOf(MediaNode child) {
    return getChildren().indexOf(child);
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

  public List<MediaNode> getChildren() {
    if(children == null) {
      if(mediaRoot == null) {
        List<MediaNode> emptyList = Collections.emptyList();
        setChildren(emptyList);
      }
      else {
        setChildren(childrenCallback.call(mediaRoot));
      }
    }

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

  // Whether or not the top most level of the tree should be displayed as tabs
  public boolean expandTopLevel() {
    return showTopLevelExpanded;
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
