package hs.mediasystem.framework;

import hs.ui.image.ImageHandle;

public interface MediaItem {
  MediaTree getRoot();
  boolean isRoot();
  boolean isLeaf();

  String getUri();
  
  String getTitle();
  String getSubtitle();
  String getReleaseYear();
  String getPlot();

  int getSeason();
  int getEpisode();
  
  ImageHandle getBanner();
  ImageHandle getPoster();
  ImageHandle getBackground();
  
  void loadData();
  boolean isDataLoaded();
}
