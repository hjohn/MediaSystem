package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaItem;
import hs.mediasystem.framework.MediaRoot;

import java.util.List;

public interface MediaGroup {
  String getId();
  String getTitle();

  boolean showTopLevelExpanded();

  List<MediaNode> getMediaNodes(MediaRoot mediaRoot, List<? extends MediaItem> mediaItems);
}