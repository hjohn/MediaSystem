package hs.mediasystem.screens;

import hs.mediasystem.framework.MediaRoot;

import java.util.List;

/**
 * Groups Media together and represents them as MediaNodes suitable for display.
 */
public interface MediaGroup<T> {
  String getId();
  String getTitle();

  boolean showTopLevelExpanded();

  List<MediaNode> getMediaNodes(MediaRoot mediaRoot, List<? extends T> items);
}