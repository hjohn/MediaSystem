package hs.mediasystem.framework;

import hs.mediasystem.ImageHandle;
import hs.mediasystem.db.Item;

public interface MediaItem {
  MediaTree getRoot();
  boolean isRoot();
  boolean isLeaf();

  String getUri();

  String getTitle();
  String getSubtitle();
  Integer getReleaseYear();
  String getPlot();

  Integer getSeason();
  Integer getEpisode();

  ImageHandle getBanner();
  ImageHandle getPoster();
  ImageHandle getBackground();

  Item getItem();  // TODO don't want this exposed here
  void setEnriched(boolean enriched);
  boolean isEnriched();
}
