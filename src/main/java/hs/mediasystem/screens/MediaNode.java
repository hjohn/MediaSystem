package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.util.ImageHandle;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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

  private final StringProperty subtitle = new SimpleStringProperty();
  public String getSubtitle() { return subtitle.get(); }
  public StringProperty subtitleProperty() { return subtitle; }

  private final ObjectProperty<Integer> season = new SimpleObjectProperty<>();
  public Integer getSeason() { return season.get(); }
  public ObjectProperty<Integer> seasonProperty() { return season; }

  private final ObjectProperty<Integer> episode = new SimpleObjectProperty<>();
  public Integer getEpisode() { return episode.get(); }
  public ObjectProperty<Integer> episodeProperty() { return episode; }

  private final StringProperty episodeRange = new SimpleStringProperty();
  public String getEpisodeRange() { return episodeRange.get(); }
  public StringProperty episodeRangeProperty() { return episodeRange; }

  private final ObjectProperty<Integer> releaseYear = new SimpleObjectProperty<>();
  public Integer getReleaseYear() { return releaseYear.get(); }
  public ObjectProperty<Integer> releaseYearProperty() { return releaseYear; }

  private final ObjectProperty<String[]> genres;
  public String[] getGenres() { return genres.get(); }
  public ObjectProperty<String[]> genresProperty() { return genres; }

  private final StringProperty plot;
  public String getPlot() { return plot.get(); }
  public StringProperty plotProperty() { return plot; }

  private final ObjectProperty<Date> releaseDate;
  public Date getReleaseDate() { return releaseDate.get(); }
  public ObjectProperty<Date> releaseDateProperty() { return releaseDate; }

  private final ObjectProperty<ImageHandle> background;
  public ImageHandle getBackground() { return background.get(); }
  public ObjectProperty<ImageHandle> backgroundProperty() { return background; }

  private final ObjectProperty<ImageHandle> banner;
  public ImageHandle getBanner() { return banner.get(); }
  public ObjectProperty<ImageHandle> bannerProperty() { return banner; }

  private final ObjectProperty<ImageHandle> poster;
  public ImageHandle getPoster() { return poster.get(); }
  public ObjectProperty<ImageHandle> posterProperty() { return poster; }

  private final StandardLayout layout;
  private final MediaItem mediaItem;
  private final String id;
  private final String shortTitle;
  private final boolean isLeaf;

  private MediaNode parent;
  private List<MediaNode> children;

  public MediaNode(StandardLayout layout, MediaItem mediaItem) {
    assert mediaItem != null;

    this.layout = layout;
    this.mediaItem = mediaItem;
    this.id = mediaItem.getUri();

    assert this.id != null;

    this.groupName.bind(mediaItem.groupNameProperty());
    this.title.bind(mediaItem.titleProperty());
    this.subtitle.bind(mediaItem.subtitleProperty());
    this.season.bind(mediaItem.seasonProperty());
    this.episode.bind(mediaItem.episodeProperty());
    this.episodeRange.bind(mediaItem.episodeRangeProperty());
    this.releaseYear.bind(mediaItem.releaseYearProperty());
    this.genres = mediaItem.genresProperty();
    this.plot = mediaItem.plotProperty();
    this.releaseDate = mediaItem.releaseDateProperty();
    this.background = mediaItem.backgroundProperty();
    this.banner = mediaItem.bannerProperty();
    this.poster = mediaItem.posterProperty();

    this.shortTitle = "";
    this.isLeaf = layout.isRoot(mediaItem) || mediaItem.isLeaf();
  }

  public MediaNode(StandardLayout layout, String groupName, String title, Integer releaseYear, Integer season) {
    this.layout = layout;
    this.shortTitle = season == null ? "" : (season == 0 ? "Sp." : "" + season);
    this.mediaItem = null;

    this.groupName.set(groupName);
    this.title.set(title);
    this.subtitle.set("");
    this.releaseYear.set(releaseYear);
    this.season.set(season);

    this.genres = new SimpleObjectProperty<>();
    this.plot = new SimpleStringProperty();
    this.releaseDate = new SimpleObjectProperty<>();
    this.background = new SimpleObjectProperty<>();
    this.banner = new SimpleObjectProperty<>();
    this.poster = new SimpleObjectProperty<>();

    this.id = "MediaNode://" + title + "/" + releaseYear + "/" + season;

    this.isLeaf = false;
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
    return isLeaf;
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
