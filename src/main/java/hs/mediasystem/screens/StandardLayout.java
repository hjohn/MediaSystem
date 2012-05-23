package hs.mediasystem.screens;

import hs.mediasystem.framework.ConfigurableCell;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.fs.EpisodeComparator;
import hs.mediasystem.fs.MediaItemComparator;
import hs.mediasystem.fs.SeasonGrouper;
import hs.mediasystem.fs.MovieGrouper;
import hs.mediasystem.media.Episode;
import hs.mediasystem.media.Media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StandardLayout {

  public ConfigurableCell<MediaNode> getCellProvider(MediaItem parent) {
    String mediaType = parent.getMediaType();

    if(mediaType.equals("Serie")) {
      return new EpisodeCell();
    }
    else if(mediaType.equals("SERIE_ROOT")) {
      return new BannerCell();
    }
    else if(mediaType.equals("MOVIES_ROOT")) {
      return new MovieCell();
    }

    return new StandardCell();
  }

  public List<MediaNode> getChildren(MediaNode parentNode) {
    MediaItem parentItem = parentNode.getMediaItem();
    List<? extends MediaItem> children = parentItem.children();
    List<MediaNode> output = new ArrayList<>();

    if(parentItem.getMediaType().equals("MOVIE_ROOT")) {
      Collection<List<MediaItem>> groupedItems = Groups.group(children, new MovieGrouper());

      for(List<MediaItem> group : groupedItems) {
        if(group.size() > 1) {
          Collections.sort(group, MediaItemComparator.INSTANCE);
          Media media = group.get(0).get(Media.class);
          MediaNode episodeGroupNode = new MediaNode(this, null, group.get(0).getTitle(), media.getReleaseYear(), null);

          List<MediaNode> nodeChildren = new ArrayList<>();
          for(MediaItem item : group) {
            nodeChildren.add(new MediaNode(this, item));
          }

          episodeGroupNode.setChildren(nodeChildren);

          output.add(episodeGroupNode);
        }
        else {
          output.add(new MediaNode(this, group.get(0)));
        }
      }
    }
    else if(parentItem.getMediaType().equals("Serie")) {
      Collection<List<MediaItem>> groupedItems = Groups.group(children, new SeasonGrouper());

      for(List<MediaItem> group : groupedItems) {
        Episode episode = group.get(0).get(Episode.class);
        int season = episode.getSeason() == null ? 0 : episode.getSeason();

        MediaNode seasonNode = new MediaNode(this, parentItem.getTitle(), createTitle(season), null, season);

        Collections.sort(group, EpisodeComparator.INSTANCE);
        List<MediaNode> nodeChildren = new ArrayList<>();

        for(MediaItem item : group) {
          nodeChildren.add(new MediaNode(this, item));
        }

        seasonNode.setChildren(nodeChildren);
        output.add(seasonNode);
      }
    }
    else {
      for(MediaItem child : children) {
        output.add(new MediaNode(this, child));
      }
    }

    Collections.sort(output, sortOrder.get().getComparator());

    return output;
  }

  private static String createTitle(int season) {
    return season == 0 ? "Specials" : "Season " + season;
  }

  public boolean expandTopLevel(MediaItem root) {
    return root.getMediaType().equals("Serie");
  }

  public boolean hasChildren(MediaItem mediaItem) {
    return !mediaItem.isLeaf();
  }

  public boolean isRoot(MediaItem mediaItem) {
    String mediaType = mediaItem.getMediaType();

    return mediaType.equals("MOVIE_ROOT") || mediaType.equals("SERIE_ROOT") || mediaType.equals("Serie");
  }

  public MediaNode createRootNode(MediaRoot root) {
    return new MediaNode(this, root);
  }

  private final ObservableList<GroupSet> groupSets = FXCollections.observableArrayList(new GroupSet("(ungrouped)"), new GroupSet("Decade"), new GroupSet("Genre"));
  private final ObservableList<SortOrder> sortOrders = FXCollections.observableArrayList(new SortOrder("Alphabetically", MediaNodeComparator.INSTANCE), new SortOrder("Chronologically", ChronologicalMediaNodeComparator.INSTANCE));

  private final ObjectProperty<GroupSet> groupSet = new SimpleObjectProperty<>(groupSets.get(0));
  private final ObjectProperty<SortOrder> sortOrder = new SimpleObjectProperty<>(sortOrders.get(0));

  public ObservableList<GroupSet> availableGroupSetsProperty() {
    return groupSets;
  }

  public ObjectProperty<GroupSet> groupSetProperty() {
    return groupSet;
  }

  public ObservableList<SortOrder> availableSortOrdersProperty() {
    return sortOrders;
  }

  public ObjectProperty<SortOrder> sortOrderProperty() {
    return sortOrder;
  }
}
