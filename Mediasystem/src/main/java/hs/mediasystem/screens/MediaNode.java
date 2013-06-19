package hs.mediasystem.screens;

import hs.mediasystem.entity.SimpleEntityProperty;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class MediaNode {
  public final SimpleEntityProperty<MediaData> mediaData;
  public final SimpleEntityProperty<Media<?>> media;

  private MediaGroup mediaGroup;
  private MediaItem mediaItem;

  public MediaItem getMediaItem() { return mediaItem; }

  public final StringProperty serieName;
  public final StringProperty title;
  public final StringProperty sequence;
  public final StringProperty subtitle;
  public final ObjectProperty<ObservableMap<String, Object>> properties;

  private final String id;
  private final String shortTitle;
  private final boolean isLeaf;
  private final Class<?> dataType;

  private MediaRoot mediaRoot;
  private MediaNode parent;
  private List<MediaNode> children;
  private boolean showTopLevelExpanded;

  public MediaNode(MediaItem mediaItem) {
    assert mediaItem != null;

    this.id = "mediaItem[" + mediaItem.getUri() + "]";
    this.mediaItem = mediaItem;
    this.serieName = mediaItem.serieName;
    this.title = mediaItem.title;
    this.sequence = mediaItem.sequence;
    this.subtitle = mediaItem.subtitle;
    this.mediaData = mediaItem.mediaData;
    this.media = mediaItem.media;
    this.properties = new SimpleObjectProperty<>(mediaItem.properties);

    this.shortTitle = "";
    this.isLeaf = true;
    this.dataType = mediaItem.getDataType();
    this.showTopLevelExpanded = false;

    if(mediaItem instanceof MediaRoot) {
      this.mediaRoot = (MediaRoot)mediaItem;
    }
  }

  public MediaNode(MediaRoot mediaRoot, String shortTitle, boolean showTopLevelExpanded, boolean isLeaf, MediaGroup mediaGroup) {
    String id = mediaRoot.getId().toString() + "[" + (mediaGroup != null ? mediaGroup.getId() : mediaRoot.getRootName()) + "]";
    Media<?> media = new SpecialItem(mediaRoot.getRootName());

    assert !id.contains("/");
    assert !id.contains(":");

    this.mediaGroup = mediaGroup;

    this.mediaRoot = mediaRoot;
    this.showTopLevelExpanded = showTopLevelExpanded;

    this.serieName = new SimpleStringProperty();
    this.title = new SimpleStringProperty(media.title.get());
    this.sequence = new SimpleStringProperty();
    this.subtitle = new SimpleStringProperty();
    ObservableMap<String, Object> observableHashMap = FXCollections.observableHashMap();
    this.properties =  new SimpleObjectProperty<>(observableHashMap);

    ObservableMap<Class<?>, Object> data = FXCollections.observableHashMap();

    data.put(Media.class, media);

    this.id = id;
    this.mediaData = new SimpleEntityProperty<>(this, "mediaData");
    this.showTopLevelExpanded = false;
    this.shortTitle = shortTitle == null ? media.title.get() : shortTitle;
    this.media = new SimpleEntityProperty<>(this, "media");
    this.media.set(media);

    this.isLeaf = isLeaf;
    this.dataType = Media.class;
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

  private void setChildren(List<MediaNode> children) {
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
      List<MediaNode> children = new ArrayList<>();

      if(mediaRoot != null) {
        List<? extends MediaItem> mediaItems = new ArrayList<>(mediaRoot.getItems());

        if(mediaGroup != null) {
          children.addAll(mediaGroup.getMediaNodes(mediaRoot, mediaItems));
        }
        else {
          for(MediaItem mediaItem : mediaItems) {
            children.add(new MediaNode(mediaItem));
          }
        }
      }

      setChildren(children);
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

    MediaNode other = (MediaNode)obj;

    return id.equals(other.id);
  }

  public Media<?> getMedia() {
    return media.get();
  }

  @Override
  public String toString() {
    return "MediaNode[id='" + id + "']";
  }

  public static class SpecialItem extends Media<SpecialItem> {
    public SpecialItem(String rootName) {
      super(rootName);
    }
  }
}
