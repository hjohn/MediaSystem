package hs.mediasystem.screens;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public class DefaultMediaGroup implements MediaGroup {
  private final String id;
  private final String title;
  private final Grouper<MediaItem> grouper;
  private final Comparator<MediaItem> sortComparator;
  private final boolean allowSingleItemGroups;
  private final boolean showTopLevelExpanded;

  public DefaultMediaGroup(String id, String title, Grouper<MediaItem> grouper, Comparator<MediaItem> sortComparator, boolean allowSingleItemGroups, boolean showTopLevelExpanded) {
    assert id != null && !id.contains("/") && !id.contains(":");

    this.id = id;
    this.title = title;
    this.grouper = grouper;
    this.sortComparator = sortComparator;
    this.allowSingleItemGroups = allowSingleItemGroups;
    this.showTopLevelExpanded = showTopLevelExpanded;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public Comparator<? super MediaItem> getSortComparator() {
    return sortComparator;
  }

  @Override
  public boolean isAllowedSingleItemGroups() {
    return allowSingleItemGroups;
  }

  @Override
  public Grouper<MediaItem> getGrouper() {
    return grouper;
  }

  @Override
  public String getShortTitle(MediaItem item) {
    return null;
  }

  @Override
  public boolean showTopLevelExpanded() {
    return showTopLevelExpanded;
  }

  @Override
  public Media<?> createMediaFromFirstItem(MediaItem item) {
    return null;
  }
}