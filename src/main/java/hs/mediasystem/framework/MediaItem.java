package hs.mediasystem.framework;

public interface MediaItem {
  boolean isRoot();
  MediaTree getRoot();

  String getTitle();
  String getSubtitle();

  int getSeason();
  int getEpisode();
}
