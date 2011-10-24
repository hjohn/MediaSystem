package hs.mediasystem.framework;

import hs.ui.image.ImageHandle;

public interface MediaItem {
  MediaTree getRoot();
  boolean isRoot();
  boolean isLeaf();

  String getUri();
  
  String getTitle();
  String getSubtitle();

  int getSeason();
  int getEpisode();
  
  ImageHandle getBanner();
}
