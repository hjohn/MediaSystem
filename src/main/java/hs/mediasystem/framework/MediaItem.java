package hs.mediasystem.framework;

import hs.mediasystem.ImageHandle;
import javafx.beans.property.ReadOnlyObjectProperty;

public interface MediaItem {
  public enum State {BASIC, ENRICHING, ENRICHED}

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

  ReadOnlyObjectProperty<State> stateProperty();
  void loadData();
}
