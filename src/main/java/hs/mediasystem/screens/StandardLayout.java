package hs.mediasystem.screens;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.Groups;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;
import hs.mediasystem.media.Media;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

public class StandardLayout {
  private static final Map<Class<? extends MediaRoot>, MediaGroup> MEDIA_GROUPS = new HashMap<>();

  public static void registerMediaGroup(Class<? extends MediaRoot> mediaRootClass, MediaGroup mediaGroup) {
    MEDIA_GROUPS.put(mediaRootClass, mediaGroup);
  }

  private List<MediaNode> getChildren(MediaRoot mediaRoot) {
    List<? extends MediaItem> children = mediaRoot.getItems();
    List<MediaNode> output = new ArrayList<>();

    MediaGroup mediaGroup = MEDIA_GROUPS.get(mediaRoot.getClass());

    if(mediaGroup != null) {
      output.addAll(applyGroup(children, mediaGroup));
    }
    else {
      for(MediaItem child : children) {
        output.add(new MediaNode(child));
      }
    }

    Collections.sort(output, sortOrder.get().getComparator());

    return output;
  }

  private List<MediaNode> applyGroup(List<? extends MediaItem> children, MediaGroup mediaGroup) {
    Collection<List<MediaItem>> groupedItems = Groups.group(children, mediaGroup.getGrouper());
    List<MediaNode> output = new ArrayList<>();

    for(List<MediaItem> group : groupedItems) {
      if(group.size() > 1 || mediaGroup.isAllowedSingleItemGroups()) {
        Collections.sort(group, mediaGroup.getSortComparator());

        Media media = mediaGroup.createMediaFromFirstItem(group.get(0));
        String shortTitle = mediaGroup.getShortTitle(group.get(0));

        MediaNode groupNode = new MediaNode(media.getTitle(), shortTitle, media.getReleaseYear());

        List<MediaNode> nodeChildren = new ArrayList<>();

        for(MediaItem item : group) {
          nodeChildren.add(new MediaNode(item));
        }

        groupNode.setChildren(nodeChildren);
        output.add(groupNode);
      }
      else {
        output.add(new MediaNode(group.get(0)));
      }
    }

    return output;
  }

  public MediaNode createRootNode(MediaRoot root) {
    MediaGroup mediaGroup = MEDIA_GROUPS.get(root.getClass());

    return new MediaNode(root, mediaGroup == null ? false : mediaGroup.showTopLevelExpanded(), new Callback<MediaRoot, List<MediaNode>>() {
      @Override
      public List<MediaNode> call(MediaRoot mediaRoot) {
        return getChildren(mediaRoot);
      }
    });
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

  public static abstract class MediaGroup {
    private final Grouper<MediaItem> grouper;
    private final Comparator<MediaItem> sortComparator;
    private final boolean allowSingleItemGroups;
    private final boolean showTopLevelExpanded;

    public MediaGroup(Grouper<MediaItem> grouper, Comparator<MediaItem> sortComparator, boolean allowSingleItemGroups, boolean showTopLevelExpanded) {
      this.grouper = grouper;
      this.sortComparator = sortComparator;
      this.allowSingleItemGroups = allowSingleItemGroups;
      this.showTopLevelExpanded = showTopLevelExpanded;
    }

    public Comparator<? super MediaItem> getSortComparator() {
      return sortComparator;
    }

    public boolean isAllowedSingleItemGroups() {
      return allowSingleItemGroups;
    }

    public Grouper<MediaItem> getGrouper() {
      return grouper;
    }

    public abstract Media createMediaFromFirstItem(MediaItem item);

    /**
     * @param item a MediaItem
     */
    public String getShortTitle(MediaItem item) {
      return null;
    }

    public boolean showTopLevelExpanded() {
      return showTopLevelExpanded;
    }
  }
}
