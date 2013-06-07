package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;

import java.util.List;

public interface MediaGroup {
  String getId();
  String getTitle();

  boolean showTopLevelExpanded();

  List<MediaNode> getMediaNodes(List<? extends MediaItem> mediaItems);
}