package hs.mediasystem.screens;

import hs.mediasystem.framework.Grouper;
import hs.mediasystem.framework.Media;
import hs.mediasystem.framework.MediaItem;

import java.util.Comparator;

public interface MediaGroup {
  public enum Constants {MEDIA_ROOT_CLASS}

  public String getTitle();

  public Comparator<? super MediaItem> getSortComparator();
  public Grouper<MediaItem> getGrouper();

  public Media createMediaFromFirstItem(MediaItem item);

  public String getShortTitle(MediaItem item);

  public boolean isAllowedSingleItemGroups();
  public boolean showTopLevelExpanded();
}