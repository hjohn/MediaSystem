package hs.mediasystem.screens;

import hs.mediasystem.framework.CellProvider;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.fs.EpisodeComparator;
import hs.mediasystem.fs.EpisodeGroup;
import hs.mediasystem.fs.MediaItemComparator;
import hs.mediasystem.fs.Season;
import hs.mediasystem.fs.SeasonGrouper;
import hs.mediasystem.fs.TitleGrouper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StandardLayout {

  public CellProvider<MediaItem> getCellProvider(MediaItem parent) {
    String mediaType = parent.getMediaType();

    if(mediaType.equals("MOVIE_ROOT")) {
      return new MovieCellProvider();
    }
    else if(mediaType.equals("SERIE_ROOT")) {
      return new BannerCellProvider();
    }

    return new SeasonAndEpisodeCellProvider();
  }

  public List<? extends MediaItem> getChildren(MediaItem parent) {
    List<? extends MediaItem> children = parent.children();
    List<MediaItem> output = new ArrayList<>();

    if(parent.getMediaType().equals("MOVIE_ROOT")) {
      Collection<List<MediaItem>> groupedItems = Groups.group(children, new TitleGrouper());

      for(List<MediaItem> group : groupedItems) {
        if(group.size() > 1) {
          Collections.sort(group, MediaItemComparator.INSTANCE);
          EpisodeGroup g = new EpisodeGroup(parent.getMediaTree(), group);

          output.add(g);
        }
        else {
          output.add(group.get(0));
        }
      }
    }
    else if(parent.getMediaType().equals("SERIE")) {
      Collection<List<MediaItem>> groupedItems = Groups.group(children, new SeasonGrouper());

      for(List<MediaItem> group : groupedItems) {
        if(group.size() > 1) {
          MediaItem episodeOne = group.get(0);
          Season s;

          if(episodeOne.getSeason() == null) {
            s = new Season(parent.getMediaTree(), parent.getTitle(), 0);
          }
          else {
            s = new Season(parent.getMediaTree(), parent.getTitle(), episodeOne.getSeason());
          }

          Collections.sort(group, EpisodeComparator.INSTANCE);

          for(MediaItem item : group) {
            s.add(item);
          }

          output.add(s);
        }
        else {
          output.add(group.get(0));
        }
      }
    }
    else {
      output.addAll(children);
    }

    Collections.sort(output, MediaItemComparator.INSTANCE);

    return output;
  }

  public List<FilterItem> getFilterItems(MediaItem root) {
    List<FilterItem> filterItems = new ArrayList<>();

    if(root.getMediaType().equals("SERIE")) {
      for(MediaItem item : getChildren(root)) {
        if(item.getSeason() == 0) {
          filterItems.add(new FilterItem(item, "Specials", "Sp."));
        }
        else {
          filterItems.add(new FilterItem(item, "Season " + item.getSeason(), "" + item.getSeason()));
        }
      }
    }

    return filterItems;
  }

  public boolean hasChildren(MediaItem mediaItem) {
    return !mediaItem.isLeaf();
  }

  public boolean isRoot(MediaItem mediaItem) {
    String mediaType = mediaItem.getMediaType();

    return mediaType.equals("MOVIE_ROOT") || mediaType.equals("SERIE_ROOT") || mediaType.equals("SERIE");
  }

  private final ObservableList<GroupSet> groupSets = FXCollections.observableArrayList(new GroupSet("(ungrouped)"), new GroupSet("Decade"), new GroupSet("Genre"));
  private final ObservableList<SortOrder> sortOrders = FXCollections.observableArrayList(new SortOrder("Alphabetically"), new SortOrder("Chronologically"));

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
