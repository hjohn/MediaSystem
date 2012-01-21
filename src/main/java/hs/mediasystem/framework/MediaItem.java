package hs.mediasystem.framework;

import hs.mediasystem.db.LocalInfo;
import hs.mediasystem.util.ImageHandle;

public interface MediaItem {
  MediaTree getRoot();
  boolean isRoot();
  boolean isLeaf();

  String getUri();

  String getTitle();
  String getSubtitle();
  Integer getReleaseYear();
  Integer getSeason();
  Integer getEpisode();

  String getPlot();
  ImageHandle getBanner();
  ImageHandle getPoster();
  ImageHandle getBackground();

  void setPlot(String plot);
  void setBanner(byte[] banner);
  void setPoster(byte[] poster);
  void setBackground(byte[] background);

  LocalInfo getLocalInfo();
  void setEnriched(boolean enriched);
  boolean isEnriched();
}
