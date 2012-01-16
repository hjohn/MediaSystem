package hs.mediasystem.framework;

import hs.mediasystem.ImageHandle;
import hs.mediasystem.db.ItemEnricher;

public interface MediaItem {
  public enum State {BASIC, ENRICHING, ENRICHED}

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

  boolean isEnriched();
  void loadData(ItemEnricher itemEnricher);
}
