package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaNodeCell;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.media.Episode;
import hs.mediasystem.media.Media;
import hs.mediasystem.util.MapBindings;

import java.util.Collections;
import java.util.List;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MediaNode {
  private final StringProperty groupName = new SimpleStringProperty();
  public String getGroupName() { return groupName.get(); }
  public StringProperty groupNameProperty() { return groupName; }

  private final StringProperty title = new SimpleStringProperty();
  public String getTitle() { return title.get(); }
  public StringProperty titleProperty() { return title; }

  private final ObjectProperty<Integer> season = new SimpleObjectProperty<>();
  public Integer getSeason() { return season.get(); }
  public ObjectProperty<Integer> seasonProperty() { return season; }

  private final ObjectProperty<Integer> releaseYear = new SimpleObjectProperty<>();
  public Integer getReleaseYear() { return releaseYear.get(); }
  public ObjectProperty<Integer> releaseYearProperty() { return releaseYear; }

  private final ObjectProperty<MediaItem> mediaItem = new SimpleObjectProperty<>();
  public MediaItem getMediaItem() { return mediaItem.get(); }
  public ObjectProperty<MediaItem> mediaItemProperty() { return mediaItem; }

  private final StandardLayout layout;
  private final String id;
  private final String shortTitle;
  private final boolean isLeaf;

  private MediaNode parent;
  private List<MediaNode> children;

  public MediaNode(StandardLayout layout, MediaItem mediaItem) {
    assert mediaItem != null;

    this.layout = layout;
    this.mediaItem.set(mediaItem);
    this.id = mediaItem.getUri() == null ? mediaItem.getMediaType() : mediaItem.getUri();

    assert this.id != null;

    this.groupName.bind(MapBindings.selectString(mediaItem.dataMapProperty(), Episode.class, "serie", Media.class, "title"));
    this.title.bind(MapBindings.selectString(mediaItem.dataMapProperty(), Media.class, "title"));
    ObjectBinding<Integer> seasonBinding = MapBindings.select(mediaItem.dataMapProperty(), Episode.class, "season");
    this.season.bind(seasonBinding);
    ObjectBinding<Integer> releaseYearBinding = MapBindings.select(mediaItem.dataMapProperty(), Media.class, "releaseYear");
    this.releaseYear.bind(releaseYearBinding);

    this.shortTitle = "";
    this.isLeaf = layout.isRoot(mediaItem) || mediaItem.isLeaf();
  }

  public MediaNode(StandardLayout layout, String groupName, String title, Integer releaseYear, Integer season) {
    this.layout = layout;
    this.shortTitle = season == null ? "" : (season == 0 ? "Sp." : "" + season);
    this.mediaItem.set(null);

    this.groupName.set(groupName);
    this.title.set(title);
    this.releaseYear.set(releaseYear);
    this.season.set(season);

    this.id = "MediaNode://" + title + "/" + releaseYear + "/" + season;

    this.isLeaf = false;
  }

  private MediaRoot mediaRoot;

  public MediaNode(StandardLayout layout, MediaRoot mediaRoot) {
    this(layout, null, mediaRoot.getRootName(), null, null);

    this.mediaRoot = mediaRoot;
  }

  public MediaRoot getMediaRoot() {
    return mediaRoot;
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
      setChildren(layout.getChildren(this));
    }

    return Collections.unmodifiableList(children);
  }

  public boolean isLeaf() {
    return isLeaf;
  }

  public MediaNodeCell getCellProvider() {
    return layout.getCellProvider(mediaItem.get());
  }

  public boolean expandTopLevel() {
    return mediaRoot != null;
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
