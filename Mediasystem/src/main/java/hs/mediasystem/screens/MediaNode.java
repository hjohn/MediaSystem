package hs.mediasystem.screens;

import hs.mediasystem.entity.SimpleEntityProperty;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaData;
import hs.mediasystem.framework.MediaItem;

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

  private final MediaItem mediaItem;

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

  private final List<MediaNode> children;

  private MediaNode parent;

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
    this.children = new ArrayList<>();
  }

  public MediaNode(String id, String title, String shortTitle, boolean isLeaf, List<MediaNode> children) {
    Media<?> media = new SpecialItem(title);

    this.children = children == null ? new ArrayList<>() : new ArrayList<>(children);
    this.mediaItem = null;

    this.serieName = new SimpleStringProperty();
    this.title = new SimpleStringProperty(title);
    this.sequence = new SimpleStringProperty();
    this.subtitle = new SimpleStringProperty();
    ObservableMap<String, Object> observableHashMap = FXCollections.observableHashMap();
    this.properties =  new SimpleObjectProperty<>(observableHashMap);

    ObservableMap<Class<?>, Object> data = FXCollections.observableHashMap();

    data.put(Media.class, media);

    this.id = id;
    this.mediaData = new SimpleEntityProperty<>(this, "mediaData");
    this.shortTitle = shortTitle == null ? media.title.get() : shortTitle;
    this.media = new SimpleEntityProperty<>(this, "media");
    this.media.set(media);

    this.isLeaf = isLeaf;
    this.dataType = Media.class;
  }

  public MediaNode(String id, String title, String shortTitle, boolean isLeaf) {
    this(id, title, shortTitle, isLeaf, null);
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

  public Media<?> getMedia() {
    return media.get();
  }

  @Override
  public String toString() {
    return "MediaNode[id='" + id + "', instance=" + hashCode() + "]";
  }

  public static class SpecialItem extends Media<SpecialItem> {
    public SpecialItem(String rootName) {
      super(rootName);
    }
  }
}
