package hs.mediasystem.screens;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;

import java.util.Comparator;

public interface MediaGroup {
  String getId();
  String getTitle();

  Comparator<? super MediaItem> getSortComparator();
  Grouper<MediaItem> getGrouper();

  Media<?> createMediaFromFirstItem(MediaItem item);

  String getShortTitle(MediaItem item);

  boolean isAllowedSingleItemGroups();
  boolean showTopLevelExpanded();

  Class<? extends MediaRoot> getMediaRootType();
}